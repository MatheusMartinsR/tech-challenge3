package com.fiap.challenge.techChallenge3.agendamento.application.port.out;

/**
 * Porta de saída para hashing/verificação de senha, desacoplando os use
 * cases da implementação concreta (BCrypt via Spring Security).
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
