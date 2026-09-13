package com.fiap.challenge.techChallenge3.scheduling.application.usecase.auth;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.PasswordEncoderPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.TokenServicePort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.InvalidCredentialsException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(senha, user.getSenha())) {
            throw new InvalidCredentialsException();
        }

        return new AuthResult(tokenService.generateToken(user), user);
    }
}
