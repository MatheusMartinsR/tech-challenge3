package com.fiap.challenge.techChallenge3.infrastructure.messaging.event;

import com.fiap.challenge.techChallenge3.domain.model.StatusConsulta;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado pelo Serviço de Agendamento sempre que uma consulta existente é editada.
 */
public record ConsultaEditadaEvent(
        Long consultaId,
        Long pacienteId,
        Long medicoId,
        LocalDateTime dataHora,
        StatusConsulta status
) implements Serializable {
}
