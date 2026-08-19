package com.fiap.challenge.techChallenge3.web.dto.auth;

public record AuthResponse(
        String token,
        String tokenType
) {
}
