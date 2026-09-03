package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaEventPublisherPort;
import com.fiap.challenge.techChallenge3.application.port.out.EnvioLembretePort;
import com.fiap.challenge.techChallenge3.application.port.out.NotificacaoRepository;
import com.fiap.challenge.techChallenge3.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.domain.model.Role;
import com.fiap.challenge.techChallenge3.domain.model.StatusConsulta;
import com.fiap.challenge.techChallenge3.domain.model.StatusNotificacao;
import com.fiap.challenge.techChallenge3.domain.model.User;
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
    private ConsultaEventPublisherPort eventPublisher;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void deveEncaminharEventoParaDeadLetterQueueQuandoOEnvioFalhaEmTodasAsTentativas() {
        doThrow(new IllegalStateException("serviço de envio indisponível"))
                .when(envioLembretePort).enviar(any());

        User paciente = userRepository.save(User.builder()
                .nome("Paciente DLQ").email("dlq@paciente.com").senha("hash").role(Role.PACIENTE).build());
        User medico = User.builder().id(20L).nome("Medico").email("medico@hospital.com").role(Role.MEDICO).build();

        eventPublisher.publicarConsultaCriada(Consulta.builder()
                .id(777L).paciente(paciente).medico(medico)
                .dataHora(LocalDateTime.now().plusDays(1)).status(StatusConsulta.AGENDADA).build());

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

        List<Notificacao> notificacoes = notificacaoRepository.findByPacienteId(paciente.getId());
        assertThat(notificacoes).isNotEmpty();
        assertThat(notificacoes).allMatch(n -> n.getStatus() == StatusNotificacao.FALHA);
    }
}
