package com.fiap.challenge.techChallenge3.scheduling.web.dto.graphql;

import com.fiap.challenge.techChallenge3.scheduling.domain.model.User;

public record DoctorGraphQlResponse(Long id, String nome) {

    public static DoctorGraphQlResponse from(User medico) {
        return new DoctorGraphQlResponse(medico.getId(), medico.getNome());
    }
}
