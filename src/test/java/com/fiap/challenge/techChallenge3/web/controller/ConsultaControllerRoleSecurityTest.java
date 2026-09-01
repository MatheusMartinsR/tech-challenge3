package com.fiap.challenge.techChallenge3.web.controller;

import com.fiap.challenge.techChallenge3.application.usecase.consulta.EditarConsultaUseCase;
import com.fiap.challenge.techChallenge3.application.usecase.consulta.RegistrarConsultaUseCase;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.domain.model.Role;
import com.fiap.challenge.techChallenge3.domain.model.StatusConsulta;
import com.fiap.challenge.techChallenge3.domain.model.User;
import com.fiap.challenge.techChallenge3.web.dto.consulta.ConsultaRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Valida a autorização por role nos endpoints de escrita de consultas usando
 * {@code @WithMockUser}, sem passar pela emissão de JWT nem pela camada de negócio
 * (os casos de uso são mockados). Complementa o {@link ConsultaControllerIntegrationTest},
 * que cobre o mesmo controle de acesso com token real e as regras de propriedade.
 *
 * <p>Cada requisição aplica {@code testSecurityContext()}: como o
 * {@code SecurityFilterChain} é {@code STATELESS}, o Spring Security usa um repositório
 * de contexto nulo, que descartaria a autenticação instalada pelo {@code @WithMockUser}.
 * O post-processor faz essa ponte.</p>
 *
 * <p>Os endpoints de leitura ficam fora daqui de propósito: eles resolvem o usuário via
 * {@code @AuthenticationPrincipal(expression = "user")}, que depende do
 * {@code UserDetailsAdapter} da aplicação e não do principal sintético do
 * {@code @WithMockUser}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class ConsultaControllerRoleSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistrarConsultaUseCase registrarConsultaUseCase;

    @MockitoBean
    private EditarConsultaUseCase editarConsultaUseCase;

    private String requestJson;

    @BeforeEach
    void setUp() {
        User paciente = User.builder().id(10L).nome("Maria").email("maria@paciente.com").role(Role.PACIENTE).build();
        User medico = User.builder().id(20L).nome("Dra. Ana").email("ana@hospital.com").role(Role.MEDICO).build();
        Consulta consulta = Consulta.builder().id(1L).paciente(paciente).medico(medico)
                .dataHora(LocalDateTime.now().plusDays(1)).status(StatusConsulta.AGENDADA).build();

        when(registrarConsultaUseCase.execute(anyLong(), anyLong(), any(), any())).thenReturn(consulta);
        when(editarConsultaUseCase.execute(anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(consulta);

        requestJson = objectMapper.writeValueAsString(
                new ConsultaRequest(10L, 20L, LocalDateTime.now().plusDays(1), "Consulta de rotina"));
    }

    private void registrar(int statusEsperado) throws Exception {
        mockMvc.perform(post("/consultas")
                        .with(testSecurityContext())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(statusEsperado));
    }

    private void editar(int statusEsperado) throws Exception {
        mockMvc.perform(put("/consultas/{id}", 1L)
                        .with(testSecurityContext())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void enfermeiroDeveConseguirRegistrarConsulta() throws Exception {
        registrar(201);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void medicoNaoDeveConseguirRegistrarConsulta() throws Exception {
        registrar(403);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void pacienteNaoDeveConseguirRegistrarConsulta() throws Exception {
        registrar(403);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    void medicoDeveConseguirEditarConsulta() throws Exception {
        editar(200);
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    void enfermeiroNaoDeveConseguirEditarConsulta() throws Exception {
        editar(403);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void pacienteNaoDeveConseguirEditarConsulta() throws Exception {
        editar(403);
    }

    @Test
    void requisicaoSemAutenticacaoDeveRetornarNaoAutorizado() throws Exception {
        registrar(401);
    }
}
