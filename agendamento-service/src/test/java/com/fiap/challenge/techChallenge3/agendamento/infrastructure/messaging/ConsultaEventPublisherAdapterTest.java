package com.fiap.challenge.techChallenge3.agendamento.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.agendamento.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.Role;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.User;
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

/**
 * Testa o adapter do Serviço de Agendamento de forma isolada (sem broker real),
 * garantindo que os eventos são publicados na exchange/routing key corretas.
 */
@ExtendWith(MockitoExtension.class)
class ConsultaEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ConsultaEventPublisherAdapter publisher;

    private User paciente;
    private User medico;

    @BeforeEach
    void setUp() {
        publisher = new ConsultaEventPublisherAdapter(rabbitTemplate);
        paciente = User.builder().id(10L).nome("Paciente").email("paciente@hospital.com").role(Role.PACIENTE).build();
        medico = User.builder().id(20L).nome("Medico").email("medico@hospital.com").role(Role.MEDICO).build();
    }

    @Test
    void devePublicarEventoDeConsultaCriadaNaExchangeCorreta() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        Consulta consulta = Consulta.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(dataHora).status(StatusConsulta.AGENDADA).build();

        publisher.publicarConsultaCriada(consulta);

        verify(rabbitTemplate).convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.ROUTING_KEY_CONSULTA_CRIADA,
                new ConsultaCriadaEvent(1L, 10L, "Paciente", "paciente@hospital.com", 20L, dataHora));
    }

    @Test
    void devePublicarEventoDeConsultaEditadaNaExchangeCorreta() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(2);
        Consulta consulta = Consulta.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(dataHora).status(StatusConsulta.REALIZADA).build();

        publisher.publicarConsultaEditada(consulta);

        verify(rabbitTemplate).convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.ROUTING_KEY_CONSULTA_EDITADA,
                new ConsultaEditadaEvent(1L, 10L, "Paciente", "paciente@hospital.com", 20L,
                        dataHora, StatusConsulta.REALIZADA));
    }
}
