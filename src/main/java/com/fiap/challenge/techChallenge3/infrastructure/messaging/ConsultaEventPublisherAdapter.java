package com.fiap.challenge.techChallenge3.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaEventPublisherPort;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.infrastructure.messaging.event.ConsultaEditadaEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter do Serviço de Agendamento que implementa {@link ConsultaEventPublisherPort}
 * publicando eventos de domínio de consultas na exchange {@link RabbitMQConfig#CONSULTAS_EXCHANGE}.
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
                RabbitMQConfig.CONSULTAS_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_CONSULTA_CRIADA,
                new ConsultaCriadaEvent(
                        consulta.getId(),
                        consulta.getPaciente().getId(),
                        consulta.getMedico().getId(),
                        consulta.getDataHora()));
    }

    @Override
    public void publicarConsultaEditada(Consulta consulta) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CONSULTAS_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_CONSULTA_EDITADA,
                new ConsultaEditadaEvent(
                        consulta.getId(),
                        consulta.getPaciente().getId(),
                        consulta.getMedico().getId(),
                        consulta.getDataHora(),
                        consulta.getStatus()));
    }
}
