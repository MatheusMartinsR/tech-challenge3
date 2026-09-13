package com.fiap.challenge.techChallenge3.notification.infrastructure.persistence.mapper;

import com.fiap.challenge.techChallenge3.notification.domain.model.Notification;
import com.fiap.challenge.techChallenge3.notification.infrastructure.persistence.entity.NotificationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toDomain(NotificationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Notification.builder()
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

    public NotificationJpaEntity toEntity(Notification domain) {
        if (domain == null) {
            return null;
        }
        return NotificationJpaEntity.builder()
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
