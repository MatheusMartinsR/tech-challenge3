package com.fiap.challenge.techChallenge3.scheduling.application.usecase.auth;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.PasswordEncoderPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.TokenServicePort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.InvalidCredentialsException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenServicePort tokenService;

    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(userRepository, passwordEncoder, tokenService);
    }

    @Test
    void authenticatesAndReturnsLoginToken() {
        User user = User.builder().id(1L).nome("Dra. Ana").email("ana@hospital.com")
                .senha("senha-hash").role(Role.MEDICO).build();

        when(userRepository.findByEmail("ana@hospital.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "senha-hash")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("jwt-gerado");

        AuthResult result = useCase.execute("ana@hospital.com", "senha123");

        assertThat(result.token()).isEqualTo("jwt-gerado");
    }

    @Test
    void rejectsMissingUser() {
        when(userRepository.findByEmail("ana@hospital.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("ana@hospital.com", "senha-errada"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rejectsIncorrectPassword() {
        User user = User.builder().id(1L).nome("Dra. Ana").email("ana@hospital.com")
                .senha("senha-hash").role(Role.MEDICO).build();
        when(userRepository.findByEmail("ana@hospital.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha-errada", "senha-hash")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute("ana@hospital.com", "senha-errada"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
