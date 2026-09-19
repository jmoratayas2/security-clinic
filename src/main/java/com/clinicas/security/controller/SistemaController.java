package com.clinicas.security.controller;

import com.clinicas.security.dto.sistema.SistemaRequest;
import com.clinicas.security.dto.sistema.SistemaResponse;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.service.SistemaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/sistemas")
public class SistemaController {
    private final SistemaService sistemaService;
    public SistemaController(SistemaService sistemaService) { this.sistemaService = sistemaService; }

    @GetMapping
    @PreAuthorize("hasAuthority('SISTEMA_READ')")
    public List<SistemaResponse> listar() { return sistemaService.listar(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SISTEMA_READ')")
    public SistemaResponse obtener(@PathVariable Long id) { return sistemaService.obtener(id); }

    @PostMapping
    @PreAuthorize("hasAuthority('SISTEMA_CREATE')")
    public ResponseEntity<SistemaResponse> crear(@Valid @RequestBody SistemaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sistemaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SISTEMA_UPDATE')")
    public SistemaResponse actualizar(@PathVariable Long id, @Valid @RequestBody SistemaRequest request) {
        return sistemaService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('SISTEMA_UPDATE')")
    public SistemaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return sistemaService.cambiarEstado(id, request.activo());
    }
}
