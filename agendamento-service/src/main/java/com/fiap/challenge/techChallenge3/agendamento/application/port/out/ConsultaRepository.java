package com.fiap.challenge.techChallenge3.agendamento.application.port.out;

import com.fiap.challenge.techChallenge3.agendamento.domain.model.Consulta;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência de consultas.
 */
public interface ConsultaRepository {

    Consulta save(Consulta consulta);

    Optional<Consulta> findById(Long id);

    List<Consulta> findAll();

    List<Consulta> findByPacienteId(Long pacienteId);
}
