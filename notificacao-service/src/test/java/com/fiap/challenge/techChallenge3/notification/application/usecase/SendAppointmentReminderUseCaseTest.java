package com.fiap.challenge.techChallenge3.notification.application.usecase;

import com.fiap.challenge.techChallenge3.notification.application.port.out.ReminderSenderPort;
import com.fiap.challenge.techChallenge3.notification.application.port.out.NotificationRepository;
import com.fiap.challenge.techChallenge3.notification.domain.model.Recipient;
import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationStatus;
import com.fiap.challenge.techChallenge3.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendAppointmentReminderUseCaseTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 15, 14, 30);
    private static final Recipient MARIA = new Recipient("Maria", "maria@paciente.com");

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ReminderSenderPort reminderSenderPort;

    private SendAppointmentReminderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendAppointmentReminderUseCase(notificationRepository, reminderSenderPort);
    }

    private void returnSavedArgument() {
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Notification captureSavedNotification() {
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void sendsReminderAndStoresSuccessfulNotification() {
        returnSavedArgument();

        useCase.execute(1L, 10L, MARIA, DATA_HORA, NotificationType.CONSULTA_CRIADA);

        verify(reminderSenderPort).send(any(Notification.class));

        Notification salva = captureSavedNotification();
        assertThat(salva.getStatus()).isEqualTo(NotificationStatus.ENVIADA);
        assertThat(salva.getConsultaId()).isEqualTo(1L);
        assertThat(salva.getPacienteId()).isEqualTo(10L);
        assertThat(salva.getDestinatario()).isEqualTo("maria@paciente.com");
        assertThat(salva.getTipo()).isEqualTo(NotificationType.CONSULTA_CRIADA);
        assertThat(salva.getDataEnvio()).isNotNull();
    }

    @Test
    void buildsCreationMessageWithRecipientAndDate() {
        returnSavedArgument();

        useCase.execute(1L, 10L, MARIA, DATA_HORA, NotificationType.CONSULTA_CRIADA);

        assertThat(captureSavedNotification().getMensagem())
                .isEqualTo("Hello, Maria! Your appointment was scheduled for 15/10/2026 14:30.");
    }

    @Test
    void buildsUpdateMessageWithRecipientAndDate() {
        returnSavedArgument();

        useCase.execute(1L, 10L, MARIA, DATA_HORA, NotificationType.CONSULTA_EDITADA);

        assertThat(captureSavedNotification().getMensagem())
                .isEqualTo("Hello, Maria! Your appointment was rescheduled for 15/10/2026 14:30.");
    }

    @Test
    void storesFailureWithoutSendingWhenContactIsMissing() {
        returnSavedArgument();
        useCase.execute(1L, 99L, new Recipient("Sem contato", null), DATA_HORA,
                NotificationType.CONSULTA_CRIADA);

        verify(reminderSenderPort, never()).send(any());

        Notification salva = captureSavedNotification();
        assertThat(salva.getStatus()).isEqualTo(NotificationStatus.FALHA);
        assertThat(salva.getDestinatario()).isNull();
    }

    @Test
    void storesFailureWithoutSendingWhenRecipientIsAbsent() {
        returnSavedArgument();

        useCase.execute(1L, 99L, null, DATA_HORA, NotificationType.CONSULTA_CRIADA);

        verify(reminderSenderPort, never()).send(any());
        assertThat(captureSavedNotification().getStatus()).isEqualTo(NotificationStatus.FALHA);
    }

    @Test
    void storesFailureAndPropagatesDeliveryException() {
        doThrow(new IllegalStateException("provider unavailable"))
                .when(reminderSenderPort).send(any(Notification.class));

        assertThatThrownBy(() -> useCase.execute(1L, 10L, MARIA, DATA_HORA, NotificationType.CONSULTA_CRIADA))
                .isInstanceOf(IllegalStateException.class);

        assertThat(captureSavedNotification().getStatus()).isEqualTo(NotificationStatus.FALHA);
    }
}
