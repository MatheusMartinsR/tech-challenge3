package com.fiap.challenge.techChallenge3.notification.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEventos;
import com.fiap.challenge.techChallenge3.notification.application.port.out.ReminderSenderPort;
import com.fiap.challenge.techChallenge3.notification.application.port.out.NotificationRepository;
import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
@TestPropertySource(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=true",
        "spring.rabbitmq.listener.simple.retry.enabled=true",
        "spring.rabbitmq.listener.simple.retry.max-retries=2",
        "spring.rabbitmq.listener.simple.retry.initial-interval=50ms",
        "spring.rabbitmq.listener.simple.retry.multiplier=1",
        "spring.rabbitmq.listener.simple.default-requeue-rejected=false"
})
class NotificationDeadLetterQueueTest {

    private static final long PACIENTE_ID = 4242L;
    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.13-management-alpine")
            .withStartupTimeout(Duration.ofMinutes(5));

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    @MockitoBean
    private ReminderSenderPort reminderSenderPort;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void routesEventToDeadLetterQueueAfterAllRetriesFail() {
        doThrow(new IllegalStateException("delivery service unavailable"))
                .when(reminderSenderPort).send(any());

        rabbitTemplate.convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.APPOINTMENT_CREATED_ROUTING_KEY,
                new ConsultaCriadaEvent(777L, PACIENTE_ID, "Paciente DLQ", "dlq@paciente.com", 20L,
                        LocalDateTime.now().plusDays(1)));
        Message mensagemMorta = Awaitility.await()
                .atMost(Duration.ofSeconds(90))
                .until(() -> rabbitTemplate.receive(NotificationRabbitConfig.NOTIFICATION_DLQ, 1000),
                        mensagem -> mensagem != null);
        Object xDeath = mensagemMorta.getMessageProperties().getHeader("x-death");
        assertThat(xDeath).isInstanceOf(List.class);
        Map<?, ?> morte = (Map<?, ?>) ((List<?>) xDeath).get(0);
        assertThat(morte.get("reason")).hasToString("rejected");
        assertThat(morte.get("queue")).hasToString(NotificationRabbitConfig.NOTIFICATION_QUEUE);
        verify(reminderSenderPort, atLeast(2)).send(any());

        List<Notification> notificacoes = notificationRepository.findByPacienteId(PACIENTE_ID);
        assertThat(notificacoes).isNotEmpty();
        assertThat(notificacoes).allMatch(n -> n.getStatus() == NotificationStatus.FALHA);
    }
}
