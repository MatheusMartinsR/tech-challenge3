package com.fiap.challenge.techChallenge3.scheduling.web.controller;

import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.GetAppointmentUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.CancelAppointmentUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.UpdateAppointmentUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.ListAppointmentsUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.CreateAppointmentUseCase;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.scheduling.web.dto.appointment.AppointmentRequest;
import com.fiap.challenge.techChallenge3.scheduling.web.dto.appointment.AppointmentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/consultas")
public class AppointmentController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final UpdateAppointmentUseCase updateAppointmentUseCase;
    private final ListAppointmentsUseCase listAppointmentsUseCase;
    private final GetAppointmentUseCase getAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;

    public AppointmentController(CreateAppointmentUseCase createAppointmentUseCase,
                               UpdateAppointmentUseCase updateAppointmentUseCase,
                               ListAppointmentsUseCase listAppointmentsUseCase,
                               GetAppointmentUseCase getAppointmentUseCase,
                               CancelAppointmentUseCase cancelAppointmentUseCase) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.updateAppointmentUseCase = updateAppointmentUseCase;
        this.listAppointmentsUseCase = listAppointmentsUseCase;
        this.getAppointmentUseCase = getAppointmentUseCase;
        this.cancelAppointmentUseCase = cancelAppointmentUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        var appointment = createAppointmentUseCase.execute(
                request.pacienteId(), request.medicoId(), request.dataHora(), request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(appointment));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<AppointmentResponse> update(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) {
        var appointment = updateAppointmentUseCase.execute(
                id, request.pacienteId(), request.medicoId(), request.dataHora(), request.observacoes());
        return ResponseEntity.ok(AppointmentResponse.from(appointment));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ResponseEntity<List<AppointmentResponse>> list(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        List<AppointmentResponse> appointments = listAppointmentsUseCase.execute(authenticatedUser).stream()
                .map(AppointmentResponse::from)
                .toList();
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable Long id,
                                                       @AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        return ResponseEntity.ok(AppointmentResponse.from(getAppointmentUseCase.execute(id, authenticatedUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(AppointmentResponse.from(cancelAppointmentUseCase.execute(id)));
    }
}
