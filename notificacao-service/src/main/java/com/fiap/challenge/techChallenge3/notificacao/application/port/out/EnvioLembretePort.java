package com.fiap.challenge.techChallenge3.notificacao.application.port.out;

import com.fiap.challenge.techChallenge3.notificacao.domain.model.Notificacao;

/**
 * Porta de saída para o envio efetivo do lembrete ao destinatário.
 *
 * <p>A implementação atual apenas registra em log (envio simulado); trocá-la por
 * e-mail ou SMS não exige mudança no caso de uso.</p>
 */
public interface EnvioLembretePort {

    void enviar(Notificacao notificacao);
}
