package com.fiap.challenge.techChallenge3.scheduling.domain.exception;

public class ResourceAccessDeniedException extends RuntimeException {

    public ResourceAccessDeniedException(String message) {
        super(message);
    }
}
