package com.fiap.challenge.techChallenge3.notificacao.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEventos;
import com.fiap.challenge.techChallenge3.notificacao.application.port.out.EnvioLembretePort;
import com.fiap.challenge.techChallenge3.notificacao.application.port.out.NotificacaoRepository;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.StatusNotificacao;
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

/**
 * Percorre o caminho de falha do Serviço de Notificações contra um broker RabbitMQ
 * real: o envio do lembrete falha em todas as tentativas e a mensagem precisa
 * terminar na dead letter queue, e não reenfileirada em laço nem descartada.
 *
 * <p>O evento é publicado direto na exchange pelo próprio teste, e não pelo Serviço
 * de Agendamento: este módulo não depende dele: o que os une é o contrato em
 * {@code common}, e é isso que o teste exercita.</p>
 *
 * <p>As esperas entre tentativas são encurtadas para milissegundos; em produção
 * valem os tempos de {@code application.properties}.</p>
 */
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
class NotificacaoDeadLetterQueueTest {

    private static final long PACIENTE_ID = 4242L;

    @Container
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQContainer::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQContainer::getAdminPassword);
    }

    /** Substitui o adapter de envio para simular uma indisponibilidade permanente. */
    @MockitoBean
    private EnvioLembretePort envioLembretePort;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void deveEncaminharEventoParaDeadLetterQueueQuandoOEnvioFalhaEmTodasAsTentativas() {
        doThrow(new IllegalStateException("serviço de envio indisponível"))
                .when(envioLembretePort).enviar(any());

        rabbitTemplate.convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.ROUTING_KEY_CONSULTA_CRIADA,
                new ConsultaCriadaEvent(777L, PACIENTE_ID, "Paciente DLQ", "dlq@paciente.com", 20L,
                        LocalDateTime.now().plusDays(1)));

        Message mensagemMorta = Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .until(() -> rabbitTemplate.receive(NotificacaoRabbitConfig.NOTIFICACAO_DLQ, 500),
                        mensagem -> mensagem != null);

        // O broker carimba o histórico da morte ao republicar na dead letter exchange.
        Object xDeath = mensagemMorta.getMessageProperties().getHeader("x-death");
        assertThat(xDeath).isInstanceOf(List.class);
        Map<?, ?> morte = (Map<?, ?>) ((List<?>) xDeath).get(0);
        assertThat(morte.get("reason")).hasToString("rejected");
        assertThat(morte.get("queue")).hasToString(NotificacaoRabbitConfig.NOTIFICACAO_QUEUE);

        // A primeira tentativa mais as reentregas configuradas.
        verify(envioLembretePort, atLeast(2)).enviar(any());

        List<Notificacao> notificacoes = notificacaoRepository.findByPacienteId(PACIENTE_ID);
        assertThat(notificacoes).isNotEmpty();
        assertThat(notificacoes).allMatch(n -> n.getStatus() == StatusNotificacao.FALHA);
    }
}
