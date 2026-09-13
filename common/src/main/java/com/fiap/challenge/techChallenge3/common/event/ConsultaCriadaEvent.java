package com.fiap.challenge.techChallenge3.common.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento publicado pelo Serviço de Agendamento sempre que uma nova consulta é criada.
 *
 * <p>Carrega os dados de contato do paciente ({@code pacienteNome} e
 * {@code pacienteEmail}) além dos identificadores. Isso é deliberado: sem eles, o
 * consumidor precisaria consultar a tabela de usuários do Serviço de Agendamento
 * para montar o lembrete, o que significaria dois serviços lendo o mesmo banco.</p>
 */
public record ConsultaCriadaEvent(
        Long consultaId,
        Long pacienteId,
        String pacienteNome,
        String pacienteEmail,
        Long medicoId,
        LocalDateTime dataHora
) implements Serializable {
}
