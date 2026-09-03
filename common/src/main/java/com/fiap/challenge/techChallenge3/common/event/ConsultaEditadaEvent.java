package com.fiap.challenge.techChallenge3.common.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado pelo Serviço de Agendamento sempre que uma consulta existente é editada.
 *
 * <p>Assim como {@link ConsultaCriadaEvent}, carrega os dados de contato do paciente
 * para que o consumidor não precise acessar o banco do Serviço de Agendamento.</p>
 */
public record ConsultaEditadaEvent(
        Long consultaId,
        Long pacienteId,
        String pacienteNome,
        String pacienteEmail,
        Long medicoId,
        LocalDateTime dataHora,
        StatusConsulta status
) implements Serializable {
}
