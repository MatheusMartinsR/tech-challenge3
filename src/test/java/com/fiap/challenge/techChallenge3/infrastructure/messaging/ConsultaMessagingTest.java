package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaEventPublisherPort;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.domain.model.Role;
import com.fiap.challenge.techChallenge3.domain.model.StatusConsulta;
import com.fiap.challenge.techChallenge3.domain.model.User;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de ponta a ponta do domínio de mensageria: sobe um broker RabbitMQ real
 * (Testcontainers), publica um evento através do {@link ConsultaEventPublisherPort} real e
 * confirma que o {@link NotificacaoListener} (Serviço de Notificações) o consome via
 * exchange/queue/binding configurados em {@code RabbitMQConfig}.
 *
 * <p>É automaticamente ignorado (não falha) em ambientes sem Docker disponível,
 * graças a {@code @EnabledIfDockerAvailable}.</p>
 */
@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
// O perfil de teste desliga o auto-start dos listeners (para os demais testes não
// dependerem de broker); aqui precisamos religar, pois o broker real está disponível.
@TestPropertySource(properties = "spring.rabbitmq.listener.simple.auto-startup=true")
class ConsultaMessagingTest {

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @Autowired
    private ConsultaEventPublisherPort eventPublisher;

    @Autowired
    private NotificacaoListener notificacaoListener;

    @Test
    void deveConsumirEventoPublicadoEmUmBrokerRabbitMqReal() {
        int antes = notificacaoListener.getNotificacoesEnviadas();

        User paciente = User.builder().id(10L).nome("Paciente").email("paciente@hospital.com").role(Role.PACIENTE).build();
        User medico = User.builder().id(20L).nome("Medico").email("medico@hospital.com").role(Role.MEDICO).build();
        Consulta consulta = Consulta.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(LocalDateTime.now().plusDays(1)).status(StatusConsulta.AGENDADA).build();

        eventPublisher.publicarConsultaCriada(consulta);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(notificacaoListener.getNotificacoesEnviadas()).isEqualTo(antes + 1));
    }
}
