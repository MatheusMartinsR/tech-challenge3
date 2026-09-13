package com.fiap.challenge.techChallenge3.scheduling.web.dto.graphql;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;

public record PatientGraphQlResponse(Long id, String nome) {

    public static PatientGraphQlResponse from(User paciente) {
        return new PatientGraphQlResponse(paciente.getId(), paciente.getNome());
    }
}
