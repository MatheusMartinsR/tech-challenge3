package com.fiap.challenge.techChallenge3.notification.infrastructure.persistence.adapter;

import com.fiap.challenge.techChallenge3.notification.application.port.out.NotificationRepository;
import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;
import com.fiap.challenge.techChallenge3.notification.infrastructure.persistence.mapper.NotificationMapper;
import com.fiap.challenge.techChallenge3.notification.infrastructure.persistence.repository.NotificationJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationMapper mapper;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository, NotificationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notification save(Notification notification) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(notification)));
    }

    @Override
    public List<Notification> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Notification> findByPacienteId(Long pacienteId) {
        return jpaRepository.findByPacienteId(pacienteId).stream().map(mapper::toDomain).toList();
    }
}
