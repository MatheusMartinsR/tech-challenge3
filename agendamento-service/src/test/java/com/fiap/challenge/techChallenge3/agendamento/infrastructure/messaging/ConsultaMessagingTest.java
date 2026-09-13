package com.fiap.challenge.techChallenge3.agendamento.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.agendamento.application.port.out.ConsultaEventPublisherPort;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.Role;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.User;
import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEventos;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica, contra um broker RabbitMQ real (Testcontainers), o lado do contrato que
 * cabe ao Serviço de Agendamento: publicar o evento na exchange, com a routing key
 * certa e com o payload completo.
 *
 * <p>Não conhece o Serviço de Notificações — a fila usada aqui é declarada pelo
 * próprio teste. É exatamente essa a garantia que interessa: quem estiver ligado à
 * exchange recebe o evento, seja quem for.</p>
 *
 * <p>É automaticamente ignorado (não falha) em ambientes sem Docker disponível,
 * graças a {@code @EnabledIfDockerAvailable}.</p>
 */
@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
class ConsultaMessagingTest {

    private static final String FILA_DE_TESTE = "teste.consultas.publicacao";

    // Margem larga de propósito: num runner de CI a subida do broker é bem mais
    // lenta que na máquina de quem desenvolve. O tempo real continua sendo o que
    // o container leva; isto é só o teto antes de desistir.
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

    /**
     * Faz o papel de um consumidor qualquer: liga uma fila própria à exchange do
     * contrato. O {@code RabbitAdmin} auto-configurado declara estes beans no broker.
     */
    @TestConfiguration
    static class FilaDeTesteConfig {

        @Bean
        Queue filaDeTeste() {
            return QueueBuilder.nonDurable(FILA_DE_TESTE).autoDelete().build();
        }

        @Bean
        Binding bindingFilaDeTeste(Queue filaDeTeste, TopicExchange consultasExchange) {
            return BindingBuilder.bind(filaDeTeste).to(consultasExchange)
                    .with(ConsultaEventos.ROUTING_KEY_CONSULTA_CRIADA);
        }
    }

    @Autowired
    private ConsultaEventPublisherPort eventPublisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void devePublicarEventoComDadosDoPacienteParaQuemEstiverLigadoNaExchange() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        User paciente = User.builder().id(10L).nome("Paciente").email("paciente@hospital.com").role(Role.PACIENTE).build();
        User medico = User.builder().id(20L).nome("Medico").email("medico@hospital.com").role(Role.MEDICO).build();
        Consulta consulta = Consulta.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(dataHora).status(StatusConsulta.AGENDADA).build();

        eventPublisher.publicarConsultaCriada(consulta);

        // Teto generoso: se a mensagem chega, chega em milissegundos, e o teste
        // termina assim que ela aparece. O valor alto só evita falha por lentidão
        // do runner de CI — não faz o teste demorar quando tudo está bem.
        Object recebido = Awaitility.await()
                .atMost(Duration.ofSeconds(60))
                .until(() -> rabbitTemplate.receiveAndConvert(FILA_DE_TESTE, 1000), Objects::nonNull);

        assertThat(recebido).isInstanceOf(ConsultaCriadaEvent.class);
        ConsultaCriadaEvent evento = (ConsultaCriadaEvent) recebido;
        assertThat(evento.consultaId()).isEqualTo(1L);
        assertThat(evento.pacienteId()).isEqualTo(10L);
        // O contato vai no payload para que o consumidor não precise ler o banco daqui.
        assertThat(evento.pacienteNome()).isEqualTo("Paciente");
        assertThat(evento.pacienteEmail()).isEqualTo("paciente@hospital.com");
        assertThat(evento.dataHora()).isEqualTo(dataHora);
    }
}
