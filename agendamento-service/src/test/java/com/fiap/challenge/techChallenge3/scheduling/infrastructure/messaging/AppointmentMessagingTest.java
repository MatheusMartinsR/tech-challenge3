package com.fiap.challenge.techChallenge3.scheduling.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentEventPublisherPort;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
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

@SpringBootTest
@Testcontainers
@EnabledIfDockerAvailable
class AppointmentMessagingTest {

    private static final String FILA_DE_TESTE = "teste.consultas.publicacao";
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

    @TestConfiguration
    static class FilaDeTesteConfig {

        @Bean
        Queue filaDeTeste() {
            return QueueBuilder.nonDurable(FILA_DE_TESTE).autoDelete().build();
        }

        @Bean
        Binding bindingFilaDeTeste(Queue filaDeTeste, TopicExchange consultasExchange) {
            return BindingBuilder.bind(filaDeTeste).to(consultasExchange)
                    .with(ConsultaEventos.APPOINTMENT_CREATED_ROUTING_KEY);
        }
    }

    @Autowired
    private AppointmentEventPublisherPort eventPublisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishesPatientDataToExchangeSubscribers() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        User paciente = User.builder().id(10L).nome("Paciente").email("paciente@hospital.com").role(Role.PACIENTE).build();
        User medico = User.builder().id(20L).nome("Medico").email("medico@hospital.com").role(Role.MEDICO).build();
        Appointment consulta = Appointment.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(dataHora).status(StatusConsulta.AGENDADA).build();

        eventPublisher.publishAppointmentCreated(consulta);
        Object recebido = Awaitility.await()
                .atMost(Duration.ofSeconds(60))
                .until(() -> rabbitTemplate.receiveAndConvert(FILA_DE_TESTE, 1000), Objects::nonNull);

        assertThat(recebido).isInstanceOf(ConsultaCriadaEvent.class);
        ConsultaCriadaEvent evento = (ConsultaCriadaEvent) recebido;
        assertThat(evento.consultaId()).isEqualTo(1L);
        assertThat(evento.pacienteId()).isEqualTo(10L);
        assertThat(evento.pacienteNome()).isEqualTo("Paciente");
        assertThat(evento.pacienteEmail()).isEqualTo("paciente@hospital.com");
        assertThat(evento.dataHora()).isEqualTo(dataHora);
    }
}
