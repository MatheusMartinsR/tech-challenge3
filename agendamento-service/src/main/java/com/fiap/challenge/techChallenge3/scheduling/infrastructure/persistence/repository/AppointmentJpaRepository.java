package com.fiap.challenge.techChallenge3.scheduling.infrastructure.persistence.repository;

import com.fiap.challenge.techChallenge3.scheduling.infrastructure.persistence.entity.AppointmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentJpaEntity, Long> {

    List<AppointmentJpaEntity> findByPacienteId(Long pacienteId);

    @Query("""
            select (count(c) > 0)
            from AppointmentJpaEntity c
            where c.status = com.fiap.challenge.techChallenge3.common.event.StatusConsulta.AGENDADA
              and c.dataHora = :dateTime
              and (c.paciente.id = :patientId or c.medico.id = :doctorId)
              and (:excludedId is null or c.id <> :excludedId)
            """)
    boolean hasScheduleConflict(@Param("patientId") Long patientId,
                                @Param("doctorId") Long doctorId,
                                @Param("dateTime") LocalDateTime dateTime,
                                @Param("excludedId") Long excludedId);
}
