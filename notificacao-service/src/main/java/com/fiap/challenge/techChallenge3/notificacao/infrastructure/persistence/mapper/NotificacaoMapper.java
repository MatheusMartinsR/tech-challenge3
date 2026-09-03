package com.fiap.challenge.techChallenge3.notificacao.infrastructure.persistence.mapper;

import com.fiap.challenge.techChallenge3.notificacao.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.notificacao.infrastructure.persistence.entity.NotificacaoJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoMapper {

    public Notificacao toDomain(NotificacaoJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Notificacao.builder()
                .id(entity.getId())
                .consultaId(entity.getConsultaId())
                .pacienteId(entity.getPacienteId())
                .destinatario(entity.getDestinatario())
                .tipo(entity.getTipo())
                .mensagem(entity.getMensagem())
                .status(entity.getStatus())
                .dataEnvio(entity.getDataEnvio())
                .build();
    }

    public NotificacaoJpaEntity toEntity(Notificacao domain) {
        if (domain == null) {
            return null;
        }
        return NotificacaoJpaEntity.builder()
                .id(domain.getId())
                .consultaId(domain.getConsultaId())
                .pacienteId(domain.getPacienteId())
                .destinatario(domain.getDestinatario())
                .tipo(domain.getTipo())
                .mensagem(domain.getMensagem())
                .status(domain.getStatus())
                .dataEnvio(domain.getDataEnvio())
                .build();
    }
}
