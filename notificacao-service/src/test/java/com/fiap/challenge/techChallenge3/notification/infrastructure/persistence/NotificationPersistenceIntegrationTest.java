package com.fiap.challenge.techChallenge3.notification.infrastructure.persistence;

import com.fiap.challenge.techChallenge3.notification.application.port.out.NotificationRepository;
import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationStatus;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
class NotificationPersistenceIntegrationTest {

    private static final LocalDateTime SENT_AT = LocalDateTime.of(2026, 10, 15, 14, 30);
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification newNotification(Long patientId, String recipient, NotificationStatus status) {
        return Notification.builder()
                .consultaId(1L)
                .pacienteId(patientId)
                .destinatario(recipient)
                .tipo(NotificationType.CONSULTA_CRIADA)
                .mensagem("Hello, Maria! Your appointment was scheduled for 15/10/2026 14:30.")
                .status(status)
                .dataEnvio(SENT_AT)
                .build();
    }

    @Test
    void persistsAndLoadsSentNotification() {
        Notification saved = notificationRepository.save(
                newNotification(10L, "maria@paciente.com", NotificationStatus.ENVIADA));

        assertThat(saved.getId()).isNotNull();

        List<Notification> found = notificationRepository.findByPacienteId(10L);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDestinatario()).isEqualTo("maria@paciente.com");
        assertThat(found.get(0).getStatus()).isEqualTo(NotificationStatus.ENVIADA);
        assertThat(found.get(0).getTipo()).isEqualTo(NotificationType.CONSULTA_CRIADA);
        assertThat(found.get(0).getDataEnvio()).isEqualTo(SENT_AT);
        assertThat(found.get(0).getMensagem()).contains("Hello", "14:30");
    }

    @Test
    void persistsFailedNotificationWithNullRecipient() {
        notificationRepository.save(newNotification(20L, null, NotificationStatus.FALHA));

        List<Notification> found = notificationRepository.findByPacienteId(20L);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDestinatario()).isNull();
        assertThat(found.get(0).getStatus()).isEqualTo(NotificationStatus.FALHA);
    }

    @Test
    void filtersNotificationsByPatient() {
        notificationRepository.save(newNotification(30L, "a@paciente.com", NotificationStatus.ENVIADA));
        notificationRepository.save(newNotification(31L, "b@paciente.com", NotificationStatus.ENVIADA));

        assertThat(notificationRepository.findByPacienteId(30L)).hasSize(1);
        assertThat(notificationRepository.findAll()).extracting(Notification::getPacienteId).contains(30L, 31L);
    }
}
