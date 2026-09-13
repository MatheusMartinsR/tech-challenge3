package com.fiap.challenge.techChallenge3.scheduling.web.controller;

import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.CancelAppointmentUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.UpdateAppointmentUseCase;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.appointment.CreateAppointmentUseCase;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.common.event.StatusConsulta;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.scheduling.web.dto.appointment.AppointmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerRoleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateAppointmentUseCase createAppointmentUseCase;

    @MockitoBean
    private UpdateAppointmentUseCase updateAppointmentUseCase;

    @MockitoBean
    private CancelAppointmentUseCase cancelAppointmentUseCase;

    private String requestJson;

    @BeforeEach
    void setUp() {
        User paciente = User.builder().id(10L).nome("Maria").email("maria@paciente.com").role(Role.PACIENTE).build();
        User medico = User.builder().id(20L).nome("Dra. Ana").email("ana@hospital.com").role(Role.MEDICO).build();
        Appointment consulta = Appointment.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(LocalDateTime.now().plusDays(1)).status(StatusConsulta.AGENDADA).build();

        when(createAppointmentUseCase.execute(anyLong(), anyLong(), any(), any())).thenReturn(consulta);
        when(updateAppointmentUseCase.execute(anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(consulta);
        when(cancelAppointmentUseCase.execute(anyLong())).thenReturn(consulta);

        requestJson = objectMapper.writeValueAsString(
                new AppointmentRequest(10L, 20L, LocalDateTime.now().plusDays(1), "Appointment de rotina"));
    }

    private void createRequest(int statusEsperado) throws Exception {
        mockMvc.perform(post("/consultas")
                        .with(testSecurityContext())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(statusEsperado));
    }

    private void updateRequest(int statusEsperado) throws Exception {
        mockMvc.perform(put("/consultas/{id}", 1L)
                        .with(testSecurityContext())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void nurseCanCreateAppointment() throws Exception {
        createRequest(201);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void doctorCanCreateAppointment() throws Exception {
        createRequest(201);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void patientCannotCreateAppointment() throws Exception {
        createRequest(403);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void doctorCanUpdateAppointment() throws Exception {
        updateRequest(200);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void nurseCanUpdateAppointment() throws Exception {
        updateRequest(200);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void patientCannotUpdateAppointment() throws Exception {
        updateRequest(403);
    }

    private void cancelRequest(int statusEsperado) throws Exception {
        mockMvc.perform(delete("/consultas/{id}", 1L).with(testSecurityContext()))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void doctorCanCancelAppointment() throws Exception {
        cancelRequest(200);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void nurseCanCancelAppointment() throws Exception {
        cancelRequest(200);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void patientCannotCancelAppointment() throws Exception {
        cancelRequest(403);
    }

    @Test
    void unauthenticatedRequestReturnsUnauthorized() throws Exception {
        createRequest(401);
    }
}
