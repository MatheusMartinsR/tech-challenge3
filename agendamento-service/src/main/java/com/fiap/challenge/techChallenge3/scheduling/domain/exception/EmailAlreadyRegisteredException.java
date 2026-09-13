package com.fiap.challenge.techChallenge3.scheduling.domain.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: " + email);
    }
}
