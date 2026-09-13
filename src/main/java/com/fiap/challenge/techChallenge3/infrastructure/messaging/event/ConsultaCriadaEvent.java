package com.fiap.challenge.techChallenge3.infrastructure.messaging.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado pelo Serviço de Agendamento sempre que uma nova consulta é criada.
 */
public record ConsultaCriadaEvent(
        Long consultaId,
        Long pacienteId,
        Long medicoId,
        LocalDateTime dataHora
) implements Serializable {
}
