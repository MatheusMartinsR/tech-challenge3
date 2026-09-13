package com.fiap.challenge.techChallenge3.scheduling.web.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull Long pacienteId,
        @NotNull Long medicoId,
        @NotNull @Future LocalDateTime dataHora,
        String observacoes
) {
}
