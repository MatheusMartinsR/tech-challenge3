package com.fiap.challenge.techChallenge3.notification.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.notification.application.port.out.NotificationRepository;
import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationStatus;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NotificationListenerIntegrationTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 15, 14, 30);

    @Autowired
    private NotificationListener notificationListener;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void storesSentNotificationWhenEventContainsPatientContact() {
        long pacienteId = 10L;

        notificationListener.handleAppointmentCreated(new ConsultaCriadaEvent(
                1L, pacienteId, "Maria", "maria@paciente.com", 20L, DATA_HORA));

        List<Notification> notificacoes = notificationRepository.findByPacienteId(pacienteId);
        assertThat(notificacoes).hasSize(1);
        assertThat(notificacoes.get(0).getStatus()).isEqualTo(NotificationStatus.ENVIADA);
        assertThat(notificacoes.get(0).getDestinatario()).isEqualTo("maria@paciente.com");
        assertThat(notificacoes.get(0).getTipo()).isEqualTo(NotificationType.CONSULTA_CRIADA);
        assertThat(notificacoes.get(0).getMensagem()).contains("Maria", "15/10/2026");
    }

    @Test
    void storesFailureWhenEventHasNoPatientContact() {
        long pacienteSemContato = 999_999L;

        notificationListener.handleAppointmentCreated(new ConsultaCriadaEvent(
                1L, pacienteSemContato, null, null, 20L, DATA_HORA));

        List<Notification> notificacoes = notificationRepository.findByPacienteId(pacienteSemContato);
        assertThat(notificacoes).hasSize(1);
        assertThat(notificacoes.get(0).getStatus()).isEqualTo(NotificationStatus.FALHA);
        assertThat(notificacoes.get(0).getDestinatario()).isNull();
    }
}
