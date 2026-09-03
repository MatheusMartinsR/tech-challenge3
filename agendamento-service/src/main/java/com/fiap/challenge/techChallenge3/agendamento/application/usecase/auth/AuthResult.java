package com.fiap.challenge.techChallenge3.agendamento.application.usecase.auth;

/**
 * Resultado de um use case de autenticação (registro ou login).
 */
public record AuthResult(String token, String tokenType) {

    public AuthResult(String token) {
        this(token, "Bearer");
    }
}
