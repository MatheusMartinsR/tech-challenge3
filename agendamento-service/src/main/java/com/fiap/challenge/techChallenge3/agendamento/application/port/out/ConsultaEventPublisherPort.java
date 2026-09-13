package com.fiap.challenge.techChallenge3.agendamento.application.port.out;

import com.fiap.challenge.techChallenge3.agendamento.domain.model.Consulta;

/**
 * Porta de saída para publicação de eventos de domínio de consultas.
 * Implementada na infraestrutura via RabbitMQ.
 */
public interface ConsultaEventPublisherPort {

    void publicarConsultaCriada(Consulta consulta);

    void publicarConsultaEditada(Consulta consulta);
}
