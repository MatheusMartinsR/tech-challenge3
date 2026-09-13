package com.fiap.challenge.techChallenge3.scheduling.application.port.out;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(Long id);

    List<Appointment> findAll();

    List<Appointment> findByPacienteId(Long patientId);

    boolean hasScheduleConflict(Long patientId, Long doctorId, LocalDateTime dateTime, Long excludedAppointmentId);
}
