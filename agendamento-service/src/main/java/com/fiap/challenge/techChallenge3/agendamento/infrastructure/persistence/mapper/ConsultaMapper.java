package com.fiap.challenge.techChallenge3.agendamento.infrastructure.persistence.mapper;

import com.fiap.challenge.techChallenge3.agendamento.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.agendamento.infrastructure.persistence.entity.ConsultaJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ConsultaMapper {

    private final UserMapper userMapper;

    public ConsultaMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Consulta toDomain(ConsultaJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Consulta.builder()
                .id(entity.getId())
                .paciente(userMapper.toDomain(entity.getPaciente()))
                .medico(userMapper.toDomain(entity.getMedico()))
                .dataHora(entity.getDataHora())
                .observacoes(entity.getObservacoes())
                .status(entity.getStatus())
                .build();
    }

    public ConsultaJpaEntity toEntity(Consulta domain) {
        if (domain == null) {
            return null;
        }
        return ConsultaJpaEntity.builder()
                .id(domain.getId())
                .paciente(userMapper.toEntity(domain.getPaciente()))
                .medico(userMapper.toEntity(domain.getMedico()))
                .dataHora(domain.getDataHora())
                .observacoes(domain.getObservacoes())
                .status(domain.getStatus())
                .build();
    }
}
