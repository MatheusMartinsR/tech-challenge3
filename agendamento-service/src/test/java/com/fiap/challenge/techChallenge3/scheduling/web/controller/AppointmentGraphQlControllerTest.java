package com.fiap.challenge.techChallenge3.scheduling.web.controller;

import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.GetUpcomingAppointmentsUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.GetAppointmentsByPatientUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.history.GetCompleteMedicalHistoryUseCase;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.scheduling.infrastructure.security.UserDetailsAdapter;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@GraphQlTest(AppointmentGraphQlController.class)
@Import(AppointmentGraphQlControllerTest.MethodSecurityConfiguration.class)
class AppointmentGraphQlControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfiguration {
    }

    @MockitoBean
    private GetAppointmentsByPatientUseCase getAppointmentsByPatientUseCase;

    @MockitoBean
    private GetUpcomingAppointmentsUseCase getUpcomingAppointmentsUseCase;

    @MockitoBean
    private GetCompleteMedicalHistoryUseCase getCompleteMedicalHistoryUseCase;

    private final GraphQlTester graphQlTester;

    private User paciente;
    private Appointment consulta;

    @Autowired
    AppointmentGraphQlControllerTest(GraphQlTester graphQlTester) {
        this.graphQlTester = graphQlTester;
    }

    @BeforeEach
    void setUp() {
        paciente = User.builder().id(7L).nome("Maria").email("maria@example.com").role(Role.PACIENTE).build();
        User medico = User.builder().id(9L).nome("Dra. Ana").email("ana@example.com").role(Role.MEDICO).build();
        consulta = Appointment.builder()
                .id(11L)
                .paciente(paciente)
                .medico(medico)
                .dataHora(LocalDateTime.of(2026, 10, 20, 14, 30))
                .status(StatusConsulta.AGENDADA)
                .observacoes("Retorno")
                .build();

        authenticate(new UserDetailsAdapter(paciente));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void exposesAppointmentPatientAndDoctorThroughSchema() {
        when(getAppointmentsByPatientUseCase.execute(7L, StatusConsulta.AGENDADA, paciente))
                .thenReturn(List.of(consulta));

        graphQlTester.document("""
                        query {
                          consultasPorPaciente(pacienteId: "7", status: AGENDADA) {
                            id
                            dataHora
                            status
                            observacoes
                            paciente { id nome }
                            medico { id nome }
                          }
                        }
                        """)
                .execute()
                .errors().verify()
                .path("consultasPorPaciente[0].id").entity(String.class).isEqualTo("11")
                .path("consultasPorPaciente[0].dataHora").entity(String.class).isEqualTo("2026-10-20T14:30:00")
                .path("consultasPorPaciente[0].status").entity(String.class).isEqualTo("AGENDADA")
                .path("consultasPorPaciente[0].observacoes").entity(String.class).isEqualTo("Retorno")
                .path("consultasPorPaciente[0].paciente.nome").entity(String.class).isEqualTo("Maria")
                .path("consultasPorPaciente[0].medico.nome").entity(String.class).isEqualTo("Dra. Ana");
    }

    @Test
    void mapsAllThreeContractQueries() {
        when(getAppointmentsByPatientUseCase.execute(7L, null, paciente)).thenReturn(List.of(consulta));
        when(getUpcomingAppointmentsUseCase.execute(7L, paciente)).thenReturn(List.of(consulta));
        when(getCompleteMedicalHistoryUseCase.execute(7L, paciente)).thenReturn(List.of());

        graphQlTester.document("""
                        query {
                          consultasPorPaciente(pacienteId: "7") { id }
                          consultasFuturas(pacienteId: "7") { id }
                          historicoCompleto(pacienteId: "7") { id }
                        }
                        """)
                .execute()
                .errors().verify()
                .path("consultasPorPaciente").entityList(Object.class).hasSize(1)
                .path("consultasFuturas").entityList(Object.class).hasSize(1)
                .path("historicoCompleto").entityList(Object.class).hasSize(0);
    }

    @Test
    void blocksRolesOutsideAllowedProfiles() {
        UserDetailsAdapter principalSemRolePermitida = new UserDetailsAdapter(paciente) {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority("ROLE_OUTRO"));
            }
        };
        authenticate(principalSemRolePermitida);

        graphQlTester.document("""
                        query {
                          consultasPorPaciente(pacienteId: "7") { id }
                        }
                        """)
                .execute()
                .errors().satisfy(errors -> assertThat(errors).hasSize(1));

        verifyNoInteractions(getAppointmentsByPatientUseCase);
    }

    private void authenticate(UserDetailsAdapter principal) {
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
