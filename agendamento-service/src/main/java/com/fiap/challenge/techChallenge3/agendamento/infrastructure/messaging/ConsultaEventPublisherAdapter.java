package com.fiap.challenge.techChallenge3.agendamento.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.agendamento.application.port.out.ConsultaEventPublisherPort;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEditadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEventos;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter do Serviço de Agendamento que implementa {@link ConsultaEventPublisherPort}
 * publicando eventos de domínio de consultas na exchange
 * {@link ConsultaEventos#EXCHANGE}.
 *
 * <p>O nome e o e-mail do paciente vão no payload de propósito: são dados que só o
 * Serviço de Agendamento possui, e mandá-los no evento é o que dispensa o consumidor
 * de consultar o banco daqui.</p>
 */
@Component
public class ConsultaEventPublisherAdapter implements ConsultaEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    public ConsultaEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarConsultaCriada(Consulta consulta) {
        rabbitTemplate.convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.ROUTING_KEY_CONSULTA_CRIADA,
                new ConsultaCriadaEvent(
                        consulta.getId(),
                        consulta.getPaciente().getId(),
                        consulta.getPaciente().getNome(),
                        consulta.getPaciente().getEmail(),
                        consulta.getMedico().getId(),
                        consulta.getDataHora()));
    }

    @Override
    public void publicarConsultaEditada(Consulta consulta) {
        rabbitTemplate.convertAndSend(
                ConsultaEventos.EXCHANGE,
                ConsultaEventos.ROUTING_KEY_CONSULTA_EDITADA,
                new ConsultaEditadaEvent(
                        consulta.getId(),
                        consulta.getPaciente().getId(),
                        consulta.getPaciente().getNome(),
                        consulta.getPaciente().getEmail(),
                        consulta.getMedico().getId(),
                        consulta.getDataHora(),
                        consulta.getStatus()));
    }
}
