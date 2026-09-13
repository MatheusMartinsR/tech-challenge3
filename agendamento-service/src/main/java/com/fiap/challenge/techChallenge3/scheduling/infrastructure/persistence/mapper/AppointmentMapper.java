package com.fiap.challenge.techChallenge3.scheduling.infrastructure.persistence.mapper;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Appointment;
import com.fiap.challenge.techChallenge3.scheduling.infrastructure.persistence.entity.AppointmentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    private final UserMapper userMapper;

    public AppointmentMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Appointment toDomain(AppointmentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Appointment.builder()
                .id(entity.getId())
                .paciente(userMapper.toDomain(entity.getPaciente()))
                .medico(userMapper.toDomain(entity.getMedico()))
                .dataHora(entity.getDataHora())
                .observacoes(entity.getObservacoes())
                .status(entity.getStatus())
                .build();
    }

    public AppointmentJpaEntity toEntity(Appointment domain) {
        if (domain == null) {
            return null;
        }
        return AppointmentJpaEntity.builder()
                .id(domain.getId())
                .paciente(userMapper.toEntity(domain.getPaciente()))
                .medico(userMapper.toEntity(domain.getMedico()))
                .dataHora(domain.getDataHora())
                .observacoes(domain.getObservacoes())
                .status(domain.getStatus())
                .build();
    }
}
