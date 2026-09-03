package com.fiap.challenge.techChallenge3.agendamento.domain.exception;

/**
 * Lançada quando uma regra de negócio de autorização é violada
 * (ex.: paciente tentando acessar consulta de outro paciente).
 *
 * <p>Distinta da negação de acesso por role (@PreAuthorize), que é uma
 * preocupação do framework tratada na borda web/segurança.</p>
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String message) {
        super(message);
    }
}
