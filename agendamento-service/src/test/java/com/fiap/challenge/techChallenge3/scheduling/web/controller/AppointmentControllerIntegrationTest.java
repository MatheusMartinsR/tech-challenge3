package com.fiap.challenge.techChallenge3.scheduling.web.controller;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentEventPublisherPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.AppointmentRepository;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import com.fiap.challenge.techChallenge3.scheduling.infrastructure.security.JwtService;
import com.fiap.challenge.techChallenge3.scheduling.web.dto.appointment.AppointmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;
    @MockitoBean
    private AppointmentEventPublisherPort consultaEventPublisher;

    private User medico;
    private User enfermeiro;
    private User paciente;
    private User outroPaciente;

    private String tokenMedico;
    private String tokenEnfermeiro;
    private String tokenPaciente;
    private String tokenOutroPaciente;

    @BeforeEach
    void setUp() {
        medico = criarUsuario("medico@hospital.com", Role.MEDICO);
        enfermeiro = criarUsuario("enfermeiro@hospital.com", Role.ENFERMEIRO);
        paciente = criarUsuario("paciente@hospital.com", Role.PACIENTE);
        outroPaciente = criarUsuario("outro.paciente@hospital.com", Role.PACIENTE);

        tokenMedico = jwtService.generateToken(medico);
        tokenEnfermeiro = jwtService.generateToken(enfermeiro);
        tokenPaciente = jwtService.generateToken(paciente);
        tokenOutroPaciente = jwtService.generateToken(outroPaciente);
    }

    private User criarUsuario(String email, Role role) {
        User user = User.builder()
                .nome(email)
                .email(email)
                .senha(passwordEncoder.encode("senha123"))
                .role(role)
                .build();
        return userRepository.save(user);
    }

    private String consultaRequestJson(Long pacienteId, Long medicoId) throws Exception {
        AppointmentRequest request = new AppointmentRequest(
                pacienteId, medicoId, LocalDateTime.now().plusDays(1), "Appointment de rotina");
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void nurseCanCreateAppointment() throws Exception {
        mockMvc.perform(post("/consultas")
                        .header("Authorization", "Bearer " + tokenEnfermeiro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    void doctorCanCreateAppointment() throws Exception {
        mockMvc.perform(post("/consultas")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    void patientCannotCreateAppointment() throws Exception {
        mockMvc.perform(post("/consultas")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void doctorCanUpdateAppointment() throws Exception {
        Long consultaId = createRequestConsulta();

        mockMvc.perform(put("/consultas/{id}", consultaId)
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isOk());
    }

    @Test
    void nurseCanUpdateAppointment() throws Exception {
        Long consultaId = createRequestConsulta();

        mockMvc.perform(put("/consultas/{id}", consultaId)
                        .header("Authorization", "Bearer " + tokenEnfermeiro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isOk());
    }

    @Test
    void patientOnlySeesOwnAppointments() throws Exception {
        createRequestConsultaPara(paciente);
        createRequestConsultaPara(outroPaciente);

        mockMvc.perform(get("/consultas").header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pacienteId").value(paciente.getId()));
    }

    @Test
    void doctorSeesAllAppointments() throws Exception {
        createRequestConsultaPara(paciente);
        createRequestConsultaPara(outroPaciente);

        mockMvc.perform(get("/consultas").header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void patientCannotAccessAnotherPatientAppointment() throws Exception {
        Long consultaDeOutrem = createRequestConsultaPara(outroPaciente);

        mockMvc.perform(get("/consultas/{id}", consultaDeOutrem).header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCanAccessOwnAppointment() throws Exception {
        Long consultaId = createRequestConsultaPara(paciente);

        mockMvc.perform(get("/consultas/{id}", consultaId).header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isOk());
    }

    @Test
    void doctorCanCancelAppointment() throws Exception {
        Long consultaId = createRequestConsulta();

        mockMvc.perform(delete("/consultas/{id}", consultaId).header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void nurseCanCancelAppointment() throws Exception {
        Long consultaId = createRequestConsulta();

        mockMvc.perform(delete("/consultas/{id}", consultaId).header("Authorization", "Bearer " + tokenEnfermeiro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));
    }

    @Test
    void patientCannotCancelAppointment() throws Exception {
        Long consultaId = createRequestConsulta();

        mockMvc.perform(delete("/consultas/{id}", consultaId).header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotCancelAlreadyCancelledAppointment() throws Exception {
        Long consultaId = createRequestConsulta();

        mockMvc.perform(delete("/consultas/{id}", consultaId).header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/consultas/{id}", consultaId).header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isConflict());
    }

    @Test
    void cancellingMissingAppointmentReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/consultas/{id}", 999999L).header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isNotFound());
    }

    private Long createRequestConsulta() throws Exception {
        return createRequestConsultaPara(paciente);
    }

    private Long createRequestConsultaPara(User paciente) throws Exception {
        String responseBody = mockMvc.perform(post("/consultas")
                        .header("Authorization", "Bearer " + tokenEnfermeiro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asLong();
    }
}
