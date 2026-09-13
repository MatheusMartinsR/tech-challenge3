package com.fiap.challenge.techChallenge3.scheduling.web.dto.graphql;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;

import java.time.format.DateTimeFormatter;

public record AppointmentGraphQlResponse(
        Long id,
        PatientGraphQlResponse paciente,
        DoctorGraphQlResponse medico,
        String dataHora,
        StatusConsulta status,
        String observacoes
) {

    public static AppointmentGraphQlResponse from(Appointment appointment) {
        return new AppointmentGraphQlResponse(
                appointment.getId(),
                PatientGraphQlResponse.from(appointment.getPaciente()),
                DoctorGraphQlResponse.from(appointment.getMedico()),
                appointment.getDataHora().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                appointment.getStatus(),
                appointment.getObservacoes());
    }
}
