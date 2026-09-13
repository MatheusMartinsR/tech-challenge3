package com.fiap.challenge.techChallenge3.notification.application.port.out;

import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;

public interface ReminderSenderPort {

    void send(Notification notification);
}
