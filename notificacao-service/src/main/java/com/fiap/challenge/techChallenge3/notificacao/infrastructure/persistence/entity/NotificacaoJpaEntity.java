package com.fiap.challenge.techChallenge3.notificacao.infrastructure.persistence.entity;

import com.fiap.challenge.techChallenge3.notificacao.domain.model.StatusNotificacao;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.TipoNotificacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Modelo de persistência (JPA) de notificação. Mapeado de/para
 * {@code domain.model.Notificacao} por {@link com.fiap.challenge.techChallenge3.notificacao.infrastructure.persistence.mapper.NotificacaoMapper}.
 */
@Entity
@Table(name = "notificacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long consultaId;

    @Column(nullable = false)
    private Long pacienteId;

    /** Nulo quando o destinatário não pôde ser identificado (notificação com status FALHA). */
    @Column
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusNotificacao status;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;
}
