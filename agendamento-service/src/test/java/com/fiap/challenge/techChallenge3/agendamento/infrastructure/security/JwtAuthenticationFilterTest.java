package com.fiap.challenge.techChallenge3.agendamento.infrastructure.security;

import com.fiap.challenge.techChallenge3.agendamento.application.port.out.TokenServicePort;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.Role;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.User;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenServicePort tokenService;

    @Mock
    private UserDetailsService userDetailsService;

    private JwtAuthenticationFilter filter;

    private User paciente;
    private UserDetailsAdapter userDetails;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenService, userDetailsService);
        SecurityContextHolder.clearContext();
        paciente = User.builder().id(1L).nome("Paciente").email("paciente@hospital.com")
                .senha("hash").role(Role.PACIENTE).build();
        userDetails = new UserDetailsAdapter(paciente);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePermitirRequisicaoSemHeaderAutorizacaoSemAutenticar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verifyNoInteractions(tokenService, userDetailsService);
    }

    @Test
    void deveAutenticarQuandoTokenValido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenService.extractUsername("token-valido")).thenReturn("paciente@hospital.com");
        when(userDetailsService.loadUserByUsername("paciente@hospital.com")).thenReturn(userDetails);
        when(tokenService.isTokenValid("token-valido", paciente)).thenReturn(true);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("paciente@hospital.com");
        verify(chain).doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarQuandoTokenInvalido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenService.extractUsername("token-invalido")).thenReturn("paciente@hospital.com");
        when(userDetailsService.loadUserByUsername("paciente@hospital.com")).thenReturn(userDetails);
        when(tokenService.isTokenValid("token-invalido", paciente)).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarQuandoTokenServiceLancaExcecao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-malformado");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(tokenService.extractUsername("token-malformado")).thenThrow(new RuntimeException("token inválido"));

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}
