package com.fiap.challenge.techChallenge3.web.dto.consulta;

import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.domain.model.StatusConsulta;

import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long medicoId,
        String medicoNome,
        LocalDateTime dataHora,
        String observacoes,
        StatusConsulta status
) {
    public static ConsultaResponse from(Consulta consulta) {
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getPaciente().getId(),
                consulta.getPaciente().getNome(),
                consulta.getMedico().getId(),
                consulta.getMedico().getNome(),
                consulta.getDataHora(),
                consulta.getObservacoes(),
                consulta.getStatus()
        );
    }
}
