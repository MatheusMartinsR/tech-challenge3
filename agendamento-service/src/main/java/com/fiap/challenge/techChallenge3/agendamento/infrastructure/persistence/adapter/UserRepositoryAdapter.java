package com.fiap.challenge.techChallenge3.agendamento.infrastructure.persistence.adapter;

import com.fiap.challenge.techChallenge3.agendamento.application.port.out.UserRepository;
import com.fiap.challenge.techChallenge3.agendamento.domain.model.User;
import com.fiap.challenge.techChallenge3.agendamento.infrastructure.persistence.mapper.UserMapper;
import com.fiap.challenge.techChallenge3.agendamento.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter que implementa a porta {@link UserRepository} usando Spring Data JPA.
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
    }
}
