package com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceAccessDeniedException;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceNotFoundException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;

    public GetAppointmentUseCase(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public Appointment execute(Long id, User authenticatedUser) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        boolean patientAccessingAnotherPatient = authenticatedUser.getRole() == Role.PACIENTE
                && !appointment.getPaciente().getId().equals(authenticatedUser.getId());

        if (patientAccessingAnotherPatient) {
            throw new ResourceAccessDeniedException("Patients may only view their own appointments");
        }

        return appointment;
    }
}
