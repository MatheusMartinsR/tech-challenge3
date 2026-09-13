package com.fiap.challenge.techChallenge3.agendamento.domain.exception;

/**
 * Lançada quando e-mail/senha informados no login não conferem.
 *
 * <p>Substitui a dependência direta de exceções do Spring Security
 * (ex.: BadCredentialsException) na camada de aplicação.</p>
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos");
    }
}
