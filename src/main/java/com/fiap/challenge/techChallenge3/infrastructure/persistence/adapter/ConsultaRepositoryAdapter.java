package com.fiap.challenge.techChallenge3.infrastructure.persistence.adapter;

import com.fiap.challenge.techChallenge3.application.port.out.ConsultaRepository;
import com.fiap.challenge.techChallenge3.domain.model.Consulta;
import com.fiap.challenge.techChallenge3.infrastructure.persistence.mapper.ConsultaMapper;
import com.fiap.challenge.techChallenge3.infrastructure.persistence.repository.ConsultaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter que implementa a porta {@link ConsultaRepository} usando Spring Data JPA.
 */
@Component
public class ConsultaRepositoryAdapter implements ConsultaRepository {

    private final ConsultaJpaRepository jpaRepository;
    private final ConsultaMapper mapper;

    public ConsultaRepositoryAdapter(ConsultaJpaRepository jpaRepository, ConsultaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Consulta save(Consulta consulta) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(consulta)));
    }

    @Override
    public Optional<Consulta> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Consulta> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Consulta> findByPacienteId(Long pacienteId) {
        return jpaRepository.findByPacienteId(pacienteId).stream().map(mapper::toDomain).toList();
    }
}
