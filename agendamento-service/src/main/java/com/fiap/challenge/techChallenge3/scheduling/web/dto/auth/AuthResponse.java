package com.fiap.challenge.techChallenge3.scheduling.web.dto.auth;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import com.fiap.challenge.techChallenge3.scheduling.application.usecase.auth.AuthResult;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                result.token(), result.tokenType(), result.user().getId(), result.user().getNome(),
                result.user().getEmail(), result.user().getRole());
    }
}
