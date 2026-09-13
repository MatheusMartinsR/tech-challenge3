package com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment;

import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentEventPublisherPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.InvalidOperationException;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceNotFoundException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class UpdateAppointmentUseCaseTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AppointmentEventPublisherPort eventPublisher;

    private UpdateAppointmentUseCase useCase;
    private User patient;
    private User doctor;

    @BeforeEach
    void setUp() {
        useCase = new UpdateAppointmentUseCase(appointmentRepository, userRepository, eventPublisher);
        patient = User.builder().id(1L).role(Role.PACIENTE).build();
        doctor = User.builder().id(2L).role(Role.MEDICO).build();
    }

    @Test
    void updatesAndPublishesAValidAppointment() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(3);
        Appointment appointment = Appointment.builder().id(10L).status(StatusConsulta.AGENDADA).build();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = useCase.execute(10L, 1L, 2L, newDateTime, "Updated notes");

        assertThat(result.getDataHora()).isEqualTo(newDateTime);
        assertThat(result.getObservacoes()).isEqualTo("Updated notes");
        verify(appointmentRepository).hasScheduleConflict(1L, 2L, newDateTime, 10L);
        verify(eventPublisher).publishAppointmentUpdated(result);
    }

    @Test
    void rejectsMissingOrCancelledAppointments() {
        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(99L, 1L, 2L, LocalDateTime.now().plusDays(1), null))
                .isInstanceOf(ResourceNotFoundException.class);

        Appointment cancelled = Appointment.builder().id(10L).status(StatusConsulta.CANCELADA).build();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(cancelled));
        assertThatThrownBy(() -> useCase.execute(10L, 1L, 2L, LocalDateTime.now().plusDays(1), null))
                .isInstanceOf(InvalidOperationException.class);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void rejectsScheduleConflicts() {
        LocalDateTime dateTime = LocalDateTime.now().plusDays(1);
        Appointment appointment = Appointment.builder().id(10L).status(StatusConsulta.AGENDADA).build();
        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.hasScheduleConflict(1L, 2L, dateTime, 10L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(10L, 1L, 2L, dateTime, null))
                .isInstanceOf(InvalidOperationException.class);
    }
}
