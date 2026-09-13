package com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceAccessDeniedException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class GetUpcomingAppointmentsUseCase {

    private final AppointmentRepository appointmentRepository;

    public GetUpcomingAppointmentsUseCase(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> execute(Long patientId, User authenticatedUser) {
        validatePatientAccess(patientId, authenticatedUser);
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository.findByPacienteId(patientId).stream()
                .filter(appointment -> appointment.getStatus() == StatusConsulta.AGENDADA)
                .filter(appointment -> appointment.getDataHora().isAfter(now))
                .sorted(Comparator.comparing(Appointment::getDataHora))
                .toList();
    }

    private void validatePatientAccess(Long patientId, User authenticatedUser) {
        if (authenticatedUser == null
                || authenticatedUser.getRole() == Role.PACIENTE
                && !Objects.equals(authenticatedUser.getId(), patientId)) {
            throw new ResourceAccessDeniedException("Patients may only view their own appointments");
        }
    }
}
