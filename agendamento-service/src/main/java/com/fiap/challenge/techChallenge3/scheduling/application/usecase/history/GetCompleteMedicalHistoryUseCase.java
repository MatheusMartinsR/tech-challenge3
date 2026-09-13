package com.fiap.challenge.techChallenge3.scheduling.application.usecase.history;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceAccessDeniedException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class GetCompleteMedicalHistoryUseCase {

    private static final Set<StatusConsulta> HISTORY_STATUSES =
            EnumSet.of(StatusConsulta.REALIZADA, StatusConsulta.CANCELADA);

    private final AppointmentRepository appointmentRepository;

    public GetCompleteMedicalHistoryUseCase(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> execute(Long patientId, User authenticatedUser) {
        validatePatientAccess(patientId, authenticatedUser);

        return appointmentRepository.findByPacienteId(patientId).stream()
                .filter(appointment -> HISTORY_STATUSES.contains(appointment.getStatus()))
                .sorted(Comparator.comparing(Appointment::getDataHora).reversed())
                .toList();
    }

    private void validatePatientAccess(Long patientId, User authenticatedUser) {
        if (authenticatedUser == null
                || authenticatedUser.getRole() == Role.PACIENTE
                && !Objects.equals(authenticatedUser.getId(), patientId)) {
            throw new ResourceAccessDeniedException("Patients may only view their own medical history");
        }
    }
}
