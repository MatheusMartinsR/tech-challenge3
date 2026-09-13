package com.fiap.challenge.techChallenge3.scheduling.application.port.out;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;

public interface TokenServicePort {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, User user);
}
