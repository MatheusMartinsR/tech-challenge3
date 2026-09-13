package com.fiap.challenge.techChallenge3.agendamento.application.usecase.auth;

import com.fiap.challenge.techChallenge3.agendamento.application.port.out.PasswordEncoderPort;
import com.fiap.challenge.techChallenge3.agendamento.application.port.out.TokenServicePort;
import com.fiap.challenge.techChallenge3.agendamento.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.agendamento.domain.exception.CredenciaisInvalidasException;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autentica um usuário por e-mail/senha e emite um token de acesso.
 *
 * <p>A verificação de credenciais é feita aqui mesmo (via portas), sem
 * depender do mecanismo de autenticação do Spring Security.</p>
 */
@Component
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenServicePort tokenService;

    public LoginUseCase(UserRepository userRepository,
                         PasswordEncoderPort passwordEncoder,
                         TokenServicePort tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public AuthResult execute(String email, String senha) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(senha, user.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        return new AuthResult(tokenService.generateToken(user));
    }
}
