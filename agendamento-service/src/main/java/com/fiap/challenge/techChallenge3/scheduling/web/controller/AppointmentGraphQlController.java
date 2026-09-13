package com.fiap.challenge.techChallenge3.scheduling.web.controller;

import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.GetUpcomingAppointmentsUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.GetAppointmentsByPatientUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.history.GetCompleteMedicalHistoryUseCase;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.scheduling.web.dto.graphql.AppointmentGraphQlResponse;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
public class AppointmentGraphQlController {

    private final GetAppointmentsByPatientUseCase getAppointmentsByPatientUseCase;
    private final GetUpcomingAppointmentsUseCase getUpcomingAppointmentsUseCase;
    private final GetCompleteMedicalHistoryUseCase getCompleteMedicalHistoryUseCase;

    public AppointmentGraphQlController(GetAppointmentsByPatientUseCase getAppointmentsByPatientUseCase,
                                      GetUpcomingAppointmentsUseCase getUpcomingAppointmentsUseCase,
                                      GetCompleteMedicalHistoryUseCase getCompleteMedicalHistoryUseCase) {
        this.getAppointmentsByPatientUseCase = getAppointmentsByPatientUseCase;
        this.getUpcomingAppointmentsUseCase = getUpcomingAppointmentsUseCase;
        this.getCompleteMedicalHistoryUseCase = getCompleteMedicalHistoryUseCase;
    }

    @QueryMapping(name = "consultasPorPaciente")
    public List<AppointmentGraphQlResponse> appointmentsByPatient(
            @Argument Long pacienteId,
            @Argument StatusConsulta status,
            @AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        return getAppointmentsByPatientUseCase.execute(pacienteId, status, authenticatedUser).stream()
                .map(AppointmentGraphQlResponse::from)
                .toList();
    }

    @QueryMapping(name = "consultasFuturas")
    public List<AppointmentGraphQlResponse> upcomingAppointments(
            @Argument Long pacienteId,
            @AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        return getUpcomingAppointmentsUseCase.execute(pacienteId, authenticatedUser).stream()
                .map(AppointmentGraphQlResponse::from)
                .toList();
    }

    @QueryMapping(name = "historicoCompleto")
    public List<AppointmentGraphQlResponse> completeMedicalHistory(
            @Argument Long pacienteId,
            @AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        return getCompleteMedicalHistoryUseCase.execute(pacienteId, authenticatedUser).stream()
                .map(AppointmentGraphQlResponse::from)
                .toList();
    }
}
