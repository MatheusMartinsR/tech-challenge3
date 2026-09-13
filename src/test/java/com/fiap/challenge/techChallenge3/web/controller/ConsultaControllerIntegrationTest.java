package com.fiap.challenge.techChallenge3.web.controller;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaEventPublisherPort;
import com.fiap.challenge.techChallenge3.application.port.out.ConsultaRepository;
import com.fiap.challenge.techChallenge3.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.domain.model.Role;
import com.fiap.challenge.techChallenge3.domain.model.User;
import com.fiap.challenge.techChallenge3.infrastructure.security.JwtService;
import com.fiap.challenge.techChallenge3.web.dto.consulta.ConsultaRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração validando o controle de acesso por role
 * (MEDICO, ENFERMEIRO, PACIENTE) sobre os recursos de consulta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConsultaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Este teste cobre controle de acesso (RBAC), não mensageria; o publisher real
    // exigiria um broker RabbitMQ disponível, o que é testado separadamente.
    @MockitoBean
    private ConsultaEventPublisherPort consultaEventPublisher;

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
        ConsultaRequest request = new ConsultaRequest(
                pacienteId, medicoId, LocalDateTime.now().plusDays(1), "Consulta de rotina");
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void enfermeiroDeveConseguirRegistrarConsulta() throws Exception {
        mockMvc.perform(post("/consultas")
                        .header("Authorization", "Bearer " + tokenEnfermeiro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"));
    }

    @Test
    void medicoNaoDeveConseguirRegistrarConsulta() throws Exception {
        mockMvc.perform(post("/consultas")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void pacienteNaoDeveConseguirRegistrarConsulta() throws Exception {
        mockMvc.perform(post("/consultas")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void requisicaoSemTokenDeveRetornarNaoAutorizado() throws Exception {
        mockMvc.perform(post("/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void medicoDeveConseguirEditarConsulta() throws Exception {
        Long consultaId = registrarConsulta();

        mockMvc.perform(put("/consultas/{id}", consultaId)
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isOk());
    }

    @Test
    void enfermeiroNaoDeveConseguirEditarConsulta() throws Exception {
        Long consultaId = registrarConsulta();

        mockMvc.perform(put("/consultas/{id}", consultaId)
                        .header("Authorization", "Bearer " + tokenEnfermeiro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void pacienteDeveVisualizarApenasAsProprias() throws Exception {
        registrarConsultaPara(paciente);
        registrarConsultaPara(outroPaciente);

        mockMvc.perform(get("/consultas").header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pacienteId").value(paciente.getId()));
    }

    @Test
    void medicoDeveVisualizarTodasAsConsultas() throws Exception {
        registrarConsultaPara(paciente);
        registrarConsultaPara(outroPaciente);

        mockMvc.perform(get("/consultas").header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void pacienteNaoDeveAcessarConsultaDeOutroPaciente() throws Exception {
        Long consultaDeOutrem = registrarConsultaPara(outroPaciente);

        mockMvc.perform(get("/consultas/{id}", consultaDeOutrem).header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isForbidden());
    }

    @Test
    void pacienteDeveAcessarAPropriaConsulta() throws Exception {
        Long consultaId = registrarConsultaPara(paciente);

        mockMvc.perform(get("/consultas/{id}", consultaId).header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isOk());
    }

    private Long registrarConsulta() throws Exception {
        return registrarConsultaPara(paciente);
    }

    private Long registrarConsultaPara(User paciente) throws Exception {
        String responseBody = mockMvc.perform(post("/consultas")
                        .header("Authorization", "Bearer " + tokenEnfermeiro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(consultaRequestJson(paciente.getId(), medico.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(responseBody).get("id").asLong();
    }
}
