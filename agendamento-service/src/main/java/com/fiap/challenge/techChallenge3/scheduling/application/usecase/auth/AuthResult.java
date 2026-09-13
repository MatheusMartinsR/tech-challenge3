package com.fiap.challenge.techChallenge3.scheduling.application.usecase.auth;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;

public record AuthResult(String token, String tokenType, User user) {

    public AuthResult(String token, User user) {
        this(token, "Bearer", user);
    }
}
