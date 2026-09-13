package com.fiap.challenge.techChallenge3.notification.application.usecase;

import com.fiap.challenge.techChallenge3.notification.application.port.out.ReminderSenderPort;
import com.fiap.challenge.techChallenge3.notification.application.port.out.NotificationRepository;
import com.fiap.challenge.techChallenge3.notification.domain.model.Recipient;
import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationStatus;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class SendAppointmentReminderUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendAppointmentReminderUseCase.class);

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationRepository notificationRepository;
    private final ReminderSenderPort reminderSenderPort;

    public SendAppointmentReminderUseCase(NotificationRepository notificationRepository,
                                          ReminderSenderPort reminderSenderPort) {
        this.notificationRepository = notificationRepository;
        this.reminderSenderPort = reminderSenderPort;
    }

    public Notification execute(Long appointmentId, Long patientId, Recipient recipient,
                                LocalDateTime dateTime, NotificationType type) {
        if (isContactMissing(recipient)) {
            log.warn("Reminder for appointment {} was not sent: event has no contact for patient {}",
                    appointmentId, patientId);
            return notificationRepository.save(
                    newNotification(appointmentId, patientId, null, type, NotificationStatus.FALHA,
                            "Event has no contact data for patient " + patientId));
        }

        Notification notification = newNotification(appointmentId, patientId, recipient.email(), type,
                NotificationStatus.ENVIADA, buildMessage(recipient, dateTime, type));

        try {
            reminderSenderPort.send(notification);
        } catch (RuntimeException e) {
            log.error("Failed to send reminder for appointment {} to patient {}", appointmentId, patientId, e);
            notification.setStatus(NotificationStatus.FALHA);
            notificationRepository.save(notification);
            throw e;
        }

        return notificationRepository.save(notification);
    }

    private boolean isContactMissing(Recipient recipient) {
        return recipient == null
                || recipient.email() == null
                || recipient.email().isBlank();
    }

    private Notification newNotification(Long appointmentId, Long patientId, String recipient,
                                         NotificationType type, NotificationStatus status, String message) {
        return Notification.builder()
                .consultaId(appointmentId)
                .pacienteId(patientId)
                .destinatario(recipient)
                .tipo(type)
                .mensagem(message)
                .status(status)
                .dataEnvio(LocalDateTime.now())
                .build();
    }

    private String buildMessage(Recipient recipient, LocalDateTime dateTime, NotificationType type) {
        String action = type == NotificationType.CONSULTA_CRIADA ? "was scheduled" : "was rescheduled";
        return "Hello, %s! Your appointment %s for %s.".formatted(
                recipient.nome(), action, DATE_TIME_FORMAT.format(dateTime));
    }
}
