package com.fiap.challenge.techChallenge3.notification.infrastructure.delivery;

import com.fiap.challenge.techChallenge3.notification.application.port.out.ReminderSenderPort;
import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingReminderSenderAdapter implements ReminderSenderPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingReminderSenderAdapter.class);

    @Override
    public void send(Notification notification) {
        log.info("[REMINDER] recipient={} | appointment={} | {}",
                notification.getDestinatario(), notification.getConsultaId(), notification.getMensagem());
    }
}
