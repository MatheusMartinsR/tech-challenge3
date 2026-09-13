package com.fiap.challenge.techChallenge3.infrastructure.persistence.repository;

import com.fiap.challenge.techChallenge3.infrastructure.persistence.entity.ConsultaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaJpaRepository extends JpaRepository<ConsultaJpaEntity, Long> {

    List<ConsultaJpaEntity> findByPacienteId(Long pacienteId);
}
