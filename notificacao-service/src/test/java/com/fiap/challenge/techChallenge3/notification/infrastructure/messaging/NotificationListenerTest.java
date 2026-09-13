package com.fiap.challenge.techChallenge3.notification.infrastructure.messaging;

import com.fiap.challenge.techChallenge3.common.event.ConsultaCriadaEvent;
import com.fiap.challenge.techChallenge3.common.event.ConsultaEditadaEvent;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import com.fiap.challenge.techChallenge3.notification.application.usecase.SendAppointmentReminderUseCase;
import com.fiap.challenge.techChallenge3.notification.domain.model.Recipient;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 15, 14, 30);
    private static final Recipient MARIA = new Recipient("Maria", "maria@paciente.com");

    @Mock
    private SendAppointmentReminderUseCase sendAppointmentReminderUseCase;

    private NotificationListener notificationListener;

    @BeforeEach
    void setUp() {
        notificationListener = new NotificationListener(sendAppointmentReminderUseCase);
    }

    private static ConsultaCriadaEvent eventoCriada() {
        return new ConsultaCriadaEvent(1L, 10L, "Maria", "maria@paciente.com", 20L, DATA_HORA);
    }

    private static ConsultaEditadaEvent eventoEditada(StatusConsulta status) {
        return new ConsultaEditadaEvent(1L, 10L, "Maria", "maria@paciente.com", 20L, DATA_HORA, status);
    }

    @Test
    void processesAppointmentCreatedEvent() {
        notificationListener.handleAppointmentCreated(eventoCriada());
        verify(sendAppointmentReminderUseCase).execute(1L, 10L, MARIA, DATA_HORA, NotificationType.CONSULTA_CRIADA);
        assertThat(notificationListener.getSentNotificationsCount()).isEqualTo(1);
    }

    @Test
    void processesAppointmentUpdatedEvent() {
        notificationListener.handleAppointmentUpdated(eventoEditada(StatusConsulta.REALIZADA));

        verify(sendAppointmentReminderUseCase).execute(1L, 10L, MARIA, DATA_HORA, NotificationType.CONSULTA_EDITADA);
        assertThat(notificationListener.getSentNotificationsCount()).isEqualTo(1);
    }

    @Test
    void incrementsCounterForEachReceivedEvent() {
        notificationListener.handleAppointmentCreated(eventoCriada());
        notificationListener.handleAppointmentUpdated(eventoEditada(StatusConsulta.CANCELADA));

        assertThat(notificationListener.getSentNotificationsCount()).isEqualTo(2);
    }

    @Test
    void ignoresUnknownEventWithoutCounting() {
        notificationListener.handleUnknownEvent("payload inesperado");

        verifyNoInteractions(sendAppointmentReminderUseCase);
        assertThat(notificationListener.getSentNotificationsCount()).isZero();
    }
}
