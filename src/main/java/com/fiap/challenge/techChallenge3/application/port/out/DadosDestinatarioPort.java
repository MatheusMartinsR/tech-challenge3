package com.fiap.challenge.techChallenge3.application.port.out;

import com.fiap.challenge.techChallenge3.domain.model.Destinatario;

import java.util.Optional;

/**
 * Porta de saída para obter os dados de contato do paciente que receberá o lembrete.
 *
 * <p>Existe porque os eventos publicados hoje carregam apenas identificadores. Isola
 * a origem desse dado: enquanto o payload não for enriquecido, a implementação consulta
 * o repositório de usuários; depois, basta trocar o adapter.</p>
 */
public interface DadosDestinatarioPort {

    Optional<Destinatario> buscarPor(Long pacienteId);
}
