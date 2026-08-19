package com.fiap.challenge.techChallenge3.application.port.out;

import com.fiap.challenge.techChallenge3.domain.model.User;

/**
 * Porta de saída para geração/validação de tokens de autenticação.
 * Implementada na infraestrutura via JWT.
 */
public interface TokenServicePort {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, User user);
}
