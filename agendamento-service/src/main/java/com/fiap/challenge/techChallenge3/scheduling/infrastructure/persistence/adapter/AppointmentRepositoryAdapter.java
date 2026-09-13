package com.fiap.challenge.techChallenge3.scheduling.infrastructure.persistence.adapter;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.infrastructure.persistence.mapper.AppointmentMapper;
import com.fiap.challenge.techChallenge3.scheduling.infrastructure.persistence.repository.AppointmentJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Component
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository jpaRepository;
    private final AppointmentMapper mapper;

    public AppointmentRepositoryAdapter(AppointmentJpaRepository jpaRepository, AppointmentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Appointment save(Appointment appointment) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(appointment)));
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Appointment> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Appointment> findByPacienteId(Long patientId) {
        return jpaRepository.findByPacienteId(patientId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean hasScheduleConflict(Long patientId, Long doctorId, LocalDateTime dateTime,
                                       Long excludedAppointmentId) {
        return jpaRepository.hasScheduleConflict(patientId, doctorId, dateTime, excludedAppointmentId);
    }
}
