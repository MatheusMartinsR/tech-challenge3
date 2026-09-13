package com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentEventPublisherPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.InvalidOperationException;
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
class CreateAppointmentUseCaseTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AppointmentEventPublisherPort eventPublisher;

    private CreateAppointmentUseCase useCase;
    private User patient;
    private User doctor;

    @BeforeEach
    void setUp() {
        useCase = new CreateAppointmentUseCase(appointmentRepository, userRepository, eventPublisher);
        patient = User.builder().id(1L).role(Role.PACIENTE).build();
        doctor = User.builder().id(2L).role(Role.MEDICO).build();
    }

    @Test
    void createsAndPublishesAValidAppointment() {
        LocalDateTime dateTime = LocalDateTime.now().plusDays(2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Appointment result = useCase.execute(1L, 2L, dateTime, "Routine visit");

        assertThat(result.getPaciente()).isSameAs(patient);
        assertThat(result.getMedico()).isSameAs(doctor);
        assertThat(result.getDataHora()).isEqualTo(dateTime);
        verify(eventPublisher).publishAppointmentCreated(result);
    }

    @Test
    void rejectsScheduleConflicts() {
        LocalDateTime dateTime = LocalDateTime.now().plusDays(2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.hasScheduleConflict(1L, 2L, dateTime, null)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, 2L, dateTime, null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("already has an appointment");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void rejectsPastAppointmentsAndInvalidRoles() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.findById(2L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> useCase.execute(1L, 2L, LocalDateTime.now().minusMinutes(1), null))
                .isInstanceOf(InvalidOperationException.class);

        patient.setRole(Role.ENFERMEIRO);
        assertThatThrownBy(() -> useCase.execute(1L, 2L, LocalDateTime.now().plusDays(1), null))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not a patient");
    }
}
