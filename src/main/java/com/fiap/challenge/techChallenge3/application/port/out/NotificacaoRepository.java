package com.fiap.challenge.techChallenge3.application.port.out;

import com.fiap.challenge.techChallenge3.domain.model.Notificacao;

import java.util.List;

/**
 * Porta de saída para persistência dos lembretes enviados.
 */
public interface NotificacaoRepository {

    Notificacao save(Notificacao notificacao);

    List<Notificacao> findAll();

    List<Notificacao> findByPacienteId(Long pacienteId);
}
