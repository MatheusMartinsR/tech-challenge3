package com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment;

import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.history.GetCompleteMedicalHistoryUseCase;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.ResourceAccessDeniedException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentQueryUseCasesTest {

    @Mock private AppointmentRepository repository;
    private User patient;
    private User nurse;

    @BeforeEach
    void setUp() {
        patient = User.builder().id(1L).role(Role.PACIENTE).build();
        nurse = User.builder().id(2L).role(Role.ENFERMEIRO).build();
    }

    @Test
    void listsAppointmentsAccordingToTheAuthenticatedRole() {
        Appointment appointment = appointment(1L, StatusConsulta.AGENDADA, LocalDateTime.now().plusDays(1));
        when(repository.findByPacienteId(1L)).thenReturn(List.of(appointment));
        assertThat(new ListAppointmentsUseCase(repository).execute(patient)).containsExactly(appointment);

        when(repository.findAll()).thenReturn(List.of(appointment));
        assertThat(new ListAppointmentsUseCase(repository).execute(nurse)).containsExactly(appointment);
    }

    @Test
    void getsOneAppointmentAndProtectsPatientOwnership() {
        Appointment appointment = appointment(1L, StatusConsulta.AGENDADA, LocalDateTime.now().plusDays(1));
        when(repository.findById(10L)).thenReturn(Optional.of(appointment));
        GetAppointmentUseCase useCase = new GetAppointmentUseCase(repository);

        assertThat(useCase.execute(10L, patient)).isSameAs(appointment);
        User anotherPatient = User.builder().id(99L).role(Role.PACIENTE).build();
        assertThatThrownBy(() -> useCase.execute(10L, anotherPatient))
                .isInstanceOf(ResourceAccessDeniedException.class);

        when(repository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(404L, nurse)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void filtersAndSortsPatientQueries() {
        LocalDateTime now = LocalDateTime.now();
        Appointment oldCompleted = appointment(1L, StatusConsulta.REALIZADA, now.minusDays(3));
        Appointment recentCancelled = appointment(1L, StatusConsulta.CANCELADA, now.minusDays(1));
        Appointment future = appointment(1L, StatusConsulta.AGENDADA, now.plusDays(2));
        Appointment pastScheduled = appointment(1L, StatusConsulta.AGENDADA, now.minusDays(1));
        when(repository.findByPacienteId(1L)).thenReturn(List.of(oldCompleted, future, recentCancelled, pastScheduled));

        assertThat(new GetAppointmentsByPatientUseCase(repository).execute(1L, StatusConsulta.AGENDADA, patient))
                .containsExactly(future, pastScheduled);
        assertThat(new GetUpcomingAppointmentsUseCase(repository).execute(1L, patient))
                .containsExactly(future);
        assertThat(new GetCompleteMedicalHistoryUseCase(repository).execute(1L, patient))
                .containsExactly(recentCancelled, oldCompleted);
    }

    @Test
    void rejectsUnauthorizedPatientQueries() {
        User anotherPatient = User.builder().id(99L).role(Role.PACIENTE).build();

        assertThatThrownBy(() -> new GetAppointmentsByPatientUseCase(repository)
                .execute(1L, null, anotherPatient)).isInstanceOf(ResourceAccessDeniedException.class);
        assertThatThrownBy(() -> new GetUpcomingAppointmentsUseCase(repository)
                .execute(1L, anotherPatient)).isInstanceOf(ResourceAccessDeniedException.class);
        assertThatThrownBy(() -> new GetCompleteMedicalHistoryUseCase(repository)
                .execute(1L, anotherPatient)).isInstanceOf(ResourceAccessDeniedException.class);
    }

    private Appointment appointment(Long patientId, StatusConsulta status, LocalDateTime dateTime) {
        return Appointment.builder()
                .id(10L)
                .paciente(User.builder().id(patientId).role(Role.PACIENTE).build())
                .status(status)
                .dataHora(dateTime)
                .build();
    }
}
