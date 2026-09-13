package com.fiap.challenge.techChallenge3.scheduling.application.port.out;

public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
