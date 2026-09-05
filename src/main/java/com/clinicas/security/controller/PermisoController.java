package com.clinicas.security.controller;

import com.clinicas.security.dto.permiso.PermisoRequest;
import com.clinicas.security.dto.permiso.PermisoResponse;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.service.PermisoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/permisos")
public class PermisoController {
    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISO_READ')")
    public List<PermisoResponse> listar() {
        return permisoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_READ')")
    public PermisoResponse obtener(@PathVariable Long id) {
        return permisoService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISO_CREATE')")
    public ResponseEntity<PermisoResponse> crear(@Valid @RequestBody PermisoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_UPDATE')")
    public PermisoResponse actualizar(@PathVariable Long id, @Valid @RequestBody PermisoRequest request) {
        return permisoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('PERMISO_UPDATE')")
    public PermisoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return permisoService.cambiarEstado(id, request);
    }
}
