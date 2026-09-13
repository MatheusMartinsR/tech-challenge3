package com.fiap.challenge.techChallenge3.scheduling.application.port.out;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    User save(User user);
}
