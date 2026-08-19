package com.fiap.challenge.techChallenge3.application.usecase.auth;

import com.fiap.challenge.techChallenge3.application.port.out.PasswordEncoderPort;
import com.fiap.challenge.techChallenge3.application.port.out.TokenServicePort;
import com.fiap.challenge.techChallenge3.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.domain.exception.EmailJaCadastradoException;
import com.fiap.challenge.techChallenge3.domain.model.Role;
import com.fiap.challenge.techChallenge3.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenServicePort tokenService;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(userRepository, passwordEncoder, tokenService);
    }

    @Test
    void deveRegistrarNovoUsuarioERetornarToken() {
        when(userRepository.existsByEmail("ana@hospital.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senha-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateToken(any(User.class))).thenReturn("jwt-gerado");

        AuthResult result = useCase.execute("Dra. Ana", "ana@hospital.com", "senha123", Role.MEDICO);

        assertThat(result.token()).isEqualTo("jwt-gerado");
        assertThat(result.tokenType()).isEqualTo("Bearer");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User salvo = userCaptor.getValue();
        assertThat(salvo.getEmail()).isEqualTo("ana@hospital.com");
        assertThat(salvo.getSenha()).isEqualTo("senha-hash");
        assertThat(salvo.getRole()).isEqualTo(Role.MEDICO);
    }

    @Test
    void deveLancarExcecaoAoRegistrarEmailJaCadastrado() {
        when(userRepository.existsByEmail("ana@hospital.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("Dra. Ana", "ana@hospital.com", "senha123", Role.MEDICO))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(userRepository, never()).save(any());
    }
}
