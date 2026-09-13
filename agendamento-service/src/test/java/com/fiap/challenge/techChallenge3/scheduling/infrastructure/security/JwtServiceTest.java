package com.fiap.challenge.techChallenge3.scheduling.infrastructure.security;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdW5pdC10ZXN0cy1vbmx5LWRvLW5vdC11c2U=";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000);
        user = User.builder()
                .id(1L)
                .nome("Dra. Ana")
                .email("ana@hospital.com")
                .senha("hash")
                .role(Role.MEDICO)
                .build();
    }

    @Test
    void generatesTokenWithEmailAsSubject() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo(user.getEmail());
    }

    @Test
    void acceptsTokenForTheSameUser() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void rejectsTokenForDifferentUser() {
        String token = jwtService.generateToken(user);

        User outroUsuario = User.builder()
                .id(2L)
                .nome("Outro")
                .email("outro@hospital.com")
                .senha("hash")
                .role(Role.PACIENTE)
                .build();

        assertThat(jwtService.isTokenValid(token, outroUsuario)).isFalse();
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService jwtServiceComExpiracaoCurta = new JwtService(SECRET, 1);
        String token = jwtServiceComExpiracaoCurta.generateToken(user);

        Thread.sleep(10);

        assertThat(jwtServiceComExpiracaoCurta.isTokenValid(token, user)).isFalse();
    }

    @Test
    void rejectsMalformedToken() {
        assertThat(jwtService.isTokenValid("token-invalido", user)).isFalse();
    }
}
