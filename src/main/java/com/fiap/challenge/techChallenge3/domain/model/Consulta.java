package com.fiap.challenge.techChallenge3.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Consulta médica agendada entre um paciente e um médico.
 *
 * <p>Modelo de domínio puro, sem anotações de persistência.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta {

    private Long id;
    private User paciente;
    private User medico;
    private LocalDateTime dataHora;
    private String observacoes;
    private StatusConsulta status;
}
