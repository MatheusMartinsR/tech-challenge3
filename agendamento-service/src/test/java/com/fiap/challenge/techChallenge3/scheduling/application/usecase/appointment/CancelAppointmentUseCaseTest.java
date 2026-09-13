package com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentEventPublisherPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.InvalidOperationException;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceNotFoundException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentEventPublisherPort eventPublisher;

    private CancelAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelAppointmentUseCase(appointmentRepository, eventPublisher);
    }

    @Test
    void cancelsScheduledAppointmentAndPublishesEvent() {
        Appointment consulta = Appointment.builder()
                .id(1L)
                .dataHora(LocalDateTime.now().plusDays(1))
                .status(StatusConsulta.AGENDADA)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment resultado = useCase.execute(1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusConsulta.CANCELADA);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusConsulta.CANCELADA);
        verify(eventPublisher).publishAppointmentUpdated(resultado);
    }

    @Test
    void rejectsMissingAppointment() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishAppointmentUpdated(any());
    }

    @Test
    void rejectsAlreadyCancelledAppointment() {
        Appointment consulta = Appointment.builder()
                .id(1L)
                .dataHora(LocalDateTime.now().plusDays(1))
                .status(StatusConsulta.CANCELADA)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(InvalidOperationException.class);

        verify(appointmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishAppointmentUpdated(any());
    }
}
