package com.fiap.challenge.techChallenge3.scheduling.application.usecase.auth;

import com.fiap.challenge.techChallenge3.scheduling.application.port.out.PasswordEncoderPort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.TokenServicePort;
import com.fiap.challenge.techChallenge3.scheduling.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.scheduling.domain.exception.EmailAlreadyRegisteredException;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenServicePort tokenService;

    public RegisterUserUseCase(UserRepository userRepository,
                                PasswordEncoderPort passwordEncoder,
                                TokenServicePort tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public AuthResult execute(String nome, String email, String senha, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        User user = User.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .role(role)
                .build();

        user = userRepository.save(user);

        return new AuthResult(tokenService.generateToken(user), user);
    }
}
