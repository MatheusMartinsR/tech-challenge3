package com.fiap.challenge.techChallenge3.infrastructure.persistence.repository;

import com.fiap.challenge.techChallenge3.infrastructure.persistence.entity.NotificacaoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoJpaRepository extends JpaRepository<NotificacaoJpaEntity, Long> {

    List<NotificacaoJpaEntity> findByPacienteId(Long pacienteId);
}
