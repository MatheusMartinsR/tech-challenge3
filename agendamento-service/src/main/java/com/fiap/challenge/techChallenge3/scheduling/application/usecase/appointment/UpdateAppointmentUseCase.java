package com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentEventPublisherPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceNotFoundException;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.InvalidOperationException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class UpdateAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final AppointmentEventPublisherPort eventPublisher;

    public UpdateAppointmentUseCase(AppointmentRepository appointmentRepository,
                                  UserRepository userRepository,
                                  AppointmentEventPublisherPort eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Appointment execute(Long appointmentId, Long patientId, Long doctorId, LocalDateTime dateTime, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        if (appointment.getStatus() == StatusConsulta.CANCELADA) {
            throw new InvalidOperationException("A cancelled appointment cannot be edited");
        }

        User patient = findUser(patientId);
        User doctor = findUser(doctorId);
        validateAppointment(patient, doctor, dateTime, appointmentId);

        appointment.setPaciente(patient);
        appointment.setMedico(doctor);
        appointment.setDataHora(dateTime);
        appointment.setObservacoes(notes);

        appointment = appointmentRepository.save(appointment);
        eventPublisher.publishAppointmentUpdated(appointment);

        return appointment;
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private void validateAppointment(User patient, User doctor, LocalDateTime dateTime, Long excludedAppointmentId) {
        if (patient.getRole() != Role.PACIENTE) {
            throw new InvalidOperationException("The selected user is not a patient");
        }
        if (doctor.getRole() != Role.MEDICO) {
            throw new InvalidOperationException("The selected user is not a doctor");
        }
        if (dateTime == null || !dateTime.isAfter(LocalDateTime.now())) {
            throw new InvalidOperationException("The appointment date and time must be in the future");
        }
        if (appointmentRepository.hasScheduleConflict(patient.getId(), doctor.getId(), dateTime, excludedAppointmentId)) {
            throw new InvalidOperationException("The patient or doctor already has an appointment at this time");
        }
    }
}
