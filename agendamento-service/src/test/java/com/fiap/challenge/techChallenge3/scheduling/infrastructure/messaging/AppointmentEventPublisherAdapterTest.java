package com.fiap.challenge.techChallenge3.scheduling.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEventos;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEditadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private AppointmentEventPublisherAdapter publisher;

    private User paciente;
    private User medico;

    @BeforeEach
    void setUp() {
        publisher = new AppointmentEventPublisherAdapter(rabbitTemplate);
        paciente = User.builder().id(10L).nome("Paciente").email("paciente@hospital.com").role(Role.PACIENTE).build();
        medico = User.builder().id(20L).nome("Medico").email("medico@hospital.com").role(Role.MEDICO).build();
    }

    @Test
    void publishesAppointmentCreatedEventToCorrectExchange() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        Appointment consulta = Appointment.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(dataHora).status(StatusConsulta.AGENDADA).build();

        publisher.publishAppointmentCreated(consulta);

        verify(rabbitTemplate).convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.APPOINTMENT_CREATED_ROUTING_KEY,
                new ConsultaCriadaEvent(1L, 10L, "Paciente", "paciente@hospital.com", 20L, dataHora));
    }

    @Test
    void publishesAppointmentUpdatedEventToCorrectExchange() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2);
        Appointment consulta = Appointment.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(dataHora).status(StatusConsulta.REALIZADA).build();

        publisher.publishAppointmentUpdated(consulta);

        verify(rabbitTemplate).convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.APPOINTMENT_UPDATED_ROUTING_KEY,
                new ConsultaEditadaEvent(1L, 10L, "Paciente", "paciente@hospital.com", 20L,
                        dataHora, StatusConsulta.REALIZADA));
    }
}
