package com.fiap.challenge.techChallenge3.notification.infrastructure.persistence.repository;

import com.fiap.challenge.techChallenge3.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

    List<NotificationJpaEntity> findByPacienteId(Long pacienteId);
}
