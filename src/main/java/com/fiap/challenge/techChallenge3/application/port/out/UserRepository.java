package com.fiap.challenge.techChallenge3.application.port.out;

import com.fiap.challenge.techChallenge3.domain.model.User;

import java.util.Optional;

/**
 * Porta de saída para persistência de usuários. Implementada na camada de
 * infraestrutura (adapter sobre Spring Data JPA).
 */
public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    User save(User user);
}
