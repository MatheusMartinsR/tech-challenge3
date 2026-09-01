package com.fiap.challenge.techChallenge3.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de um lembrete de consulta enviado (ou tentado) ao paciente.
 *
 * <p>Modelo de domínio puro, sem anotações de persistência.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacao {

    private Long id;
    private Long consultaId;
    private Long pacienteId;
    private String destinatario;
    private TipoNotificacao tipo;
    private String mensagem;
    private StatusNotificacao status;
    private LocalDateTime dataEnvio;
}
