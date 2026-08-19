package com.fiap.challenge.techChallenge3.web.controller;

import com.fiap.challenge.techChallenge3.application.usecase.consulta.BuscarConsultaUseCase;
import com.fiap.challenge.techChallenge3.application.usecase.consulta.EditarConsultaUseCase;
import com.fiap.challenge.techChallenge3.application.usecase.consulta.ListarConsultasUseCase;
import com.fiap.challenge.techChallenge3.application.usecase.consulta.RegistrarConsultaUseCase;
import com.fiap.challenge.techChallenge3.domain.model.User;
import com.fiap.challenge.techChallenge3.web.dto.consulta.ConsultaRequest;
import com.fiap.challenge.techChallenge3.web.dto.consulta.ConsultaResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final RegistrarConsultaUseCase registrarConsultaUseCase;
    private final EditarConsultaUseCase editarConsultaUseCase;
    private final ListarConsultasUseCase listarConsultasUseCase;
    private final BuscarConsultaUseCase buscarConsultaUseCase;

    public ConsultaController(RegistrarConsultaUseCase registrarConsultaUseCase,
                               EditarConsultaUseCase editarConsultaUseCase,
                               ListarConsultasUseCase listarConsultasUseCase,
                               BuscarConsultaUseCase buscarConsultaUseCase) {
        this.registrarConsultaUseCase = registrarConsultaUseCase;
        this.editarConsultaUseCase = editarConsultaUseCase;
        this.listarConsultasUseCase = listarConsultasUseCase;
        this.buscarConsultaUseCase = buscarConsultaUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ENFERMEIRO')")
    public ResponseEntity<ConsultaResponse> registrar(@Valid @RequestBody ConsultaRequest request) {
        var consulta = registrarConsultaUseCase.execute(
                request.pacienteId(), request.medicoId(), request.dataHora(), request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsultaResponse.from(consulta));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<ConsultaResponse> editar(@PathVariable Long id, @Valid @RequestBody ConsultaRequest request) {
        var consulta = editarConsultaUseCase.execute(
                id, request.pacienteId(), request.medicoId(), request.dataHora(), request.observacoes());
        return ResponseEntity.ok(ConsultaResponse.from(consulta));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ResponseEntity<List<ConsultaResponse>> listar(
            @AuthenticationPrincipal(expression = "user") User usuarioAutenticado) {
        List<ConsultaResponse> consultas = listarConsultasUseCase.execute(usuarioAutenticado).stream()
                .map(ConsultaResponse::from)
                .toList();
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ResponseEntity<ConsultaResponse> buscarPorId(@PathVariable Long id,
                                                          @AuthenticationPrincipal(expression = "user") User usuarioAutenticado) {
        return ResponseEntity.ok(ConsultaResponse.from(buscarConsultaUseCase.execute(id, usuarioAutenticado)));
    }
}
