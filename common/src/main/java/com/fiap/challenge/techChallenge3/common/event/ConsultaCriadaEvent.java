package com.fiap.challenge.techChallenge3.common.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ConsultaCriadaEvent(
        Long consultaId,
        Long pacienteId,
        String pacienteNome,
        String pacienteEmail,
        Long medicoId,
        LocalDateTime dataHora
) implements Serializable {
}
