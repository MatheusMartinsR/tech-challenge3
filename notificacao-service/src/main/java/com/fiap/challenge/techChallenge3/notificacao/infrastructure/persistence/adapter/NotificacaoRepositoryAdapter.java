package com.fiap.challenge.techChallenge3.notificacao.infrastructure.persistence.adapter;

import com.fiap.challenge.techChallenge3.notificacao.application.port.out.NotificacaoRepository;
import com.fiap.challenge.techChallenge3.notificacao.domain.model.Notificacao;
import com.fiap.challenge.techChallenge3.notificacao.infrastructure.persistence.mapper.NotificacaoMapper;
import com.fiap.challenge.techChallenge3.notificacao.infrastructure.persistence.repository.NotificacaoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter que implementa a porta {@link NotificacaoRepository} usando Spring Data JPA.
 */
@Component
public class NotificacaoRepositoryAdapter implements NotificacaoRepository {

    private final NotificacaoJpaRepository jpaRepository;
    private final NotificacaoMapper mapper;

    public NotificacaoRepositoryAdapter(NotificacaoJpaRepository jpaRepository, NotificacaoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Notificacao save(Notificacao notificacao) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(notificacao)));
    }

    @Override
    public List<Notificacao> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Notificacao> findByPacienteId(Long pacienteId) {
        return jpaRepository.findByPacienteId(pacienteId).stream().map(mapper::toDomain).toList();
    }
}
