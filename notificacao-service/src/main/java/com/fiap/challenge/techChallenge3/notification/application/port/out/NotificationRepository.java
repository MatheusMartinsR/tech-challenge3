package com.fiap.challenge.techChallenge3.notification.application.port.out;

import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;

import java.util.List;

public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> findAll();

    List<Notification> findByPacienteId(Long pacienteId);
}
