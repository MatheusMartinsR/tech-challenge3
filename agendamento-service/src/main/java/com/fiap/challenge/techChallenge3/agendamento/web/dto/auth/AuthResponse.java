package com.fiap.challenge.techChallenge3.agendamento.web.dto.auth;

public record AuthResponse(
        String token,
        String tokenType
) {
}
