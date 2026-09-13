package com.fiap.challenge.techChallenge3.scheduling.web.dto.appointment;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long medicoId,
        String medicoNome,
        LocalDateTime dataHora,
        String observacoes,
        StatusConsulta status
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPaciente().getId(),
                appointment.getPaciente().getNome(),
                appointment.getMedico().getId(),
                appointment.getMedico().getNome(),
                appointment.getDataHora(),
                appointment.getObservacoes(),
                appointment.getStatus()
        );
    }
}
