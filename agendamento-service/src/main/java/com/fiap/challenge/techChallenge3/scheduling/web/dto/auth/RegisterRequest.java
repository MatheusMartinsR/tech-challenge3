package com.fiap.challenge.techChallenge3.scheduling.web.dto.auth;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "Password must contain at least 6 characters") String senha,
        @NotNull Role role
) {
}
