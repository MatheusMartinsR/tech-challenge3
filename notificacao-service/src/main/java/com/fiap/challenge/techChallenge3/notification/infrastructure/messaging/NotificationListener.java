package com.fiap.challenge.techChallenge3.notification.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEditadaEvent;
import com.fiap.challenge.techChallenge3.notification.application.usecase.SendAppointmentReminderUseCase;
import com.fiap.challenge.techChallenge3.notification.domain.model.Recipient;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RabbitListener(queues = NotificationRabbitConfig.NOTIFICATION_QUEUE)
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    private final SendAppointmentReminderUseCase sendAppointmentReminderUseCase;

    private final AtomicInteger sentNotifications = new AtomicInteger(0);

    public NotificationListener(SendAppointmentReminderUseCase sendAppointmentReminderUseCase) {
        this.sendAppointmentReminderUseCase = sendAppointmentReminderUseCase;
    }

    @RabbitHandler
    public void handleAppointmentCreated(ConsultaCriadaEvent event) {
        process(event.consultaId(), event.pacienteId(),
                new Recipient(event.pacienteNome(), event.pacienteEmail()),
                event.dataHora(), NotificationType.CONSULTA_CRIADA);
    }

    @RabbitHandler
    public void handleAppointmentUpdated(ConsultaEditadaEvent event) {
        process(event.consultaId(), event.pacienteId(),
                new Recipient(event.pacienteNome(), event.pacienteEmail()),
                event.dataHora(), NotificationType.CONSULTA_EDITADA);
    }

    @RabbitHandler(isDefault = true)
    public void handleUnknownEvent(Object event) {
        log.warn("Unknown appointment event received: {}", event);
    }

    private void process(Long appointmentId, Long patientId, Recipient recipient,
                         LocalDateTime dateTime, NotificationType type) {
        sendAppointmentReminderUseCase.execute(appointmentId, patientId, recipient, dateTime, type);
        sentNotifications.incrementAndGet();
    }

    public int getSentNotificationsCount() {
        return sentNotifications.get();
    }
}
