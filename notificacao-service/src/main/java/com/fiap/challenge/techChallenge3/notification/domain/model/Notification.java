package com.fiap.challenge.techChallenge3.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    private Long id;
    private Long consultaId;
    private Long pacienteId;
    private String destinatario;
    private NotificationType tipo;
    private String mensagem;
    private NotificationStatus status;
    private LocalDateTime dataEnvio;
}
