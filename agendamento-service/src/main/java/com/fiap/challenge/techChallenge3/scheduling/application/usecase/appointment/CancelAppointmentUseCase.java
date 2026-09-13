package com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentEventPublisherPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.InvalidOperationException;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceNotFoundException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CancelAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentEventPublisherPort eventPublisher;

    public CancelAppointmentUseCase(AppointmentRepository appointmentRepository,
                                    AppointmentEventPublisherPort eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Appointment execute(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        if (appointment.getStatus() == StatusConsulta.CANCELADA) {
            throw new InvalidOperationException("Appointment is already cancelled: " + appointmentId);
        }

        appointment.setStatus(StatusConsulta.CANCELADA);
        appointment = appointmentRepository.save(appointment);
        eventPublisher.publishAppointmentUpdated(appointment);

        return appointment;
    }
}
