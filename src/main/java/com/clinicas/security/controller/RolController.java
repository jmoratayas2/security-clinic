package com.clinicas.security.controller;

import com.clinicas.security.dto.rol.RolRequest;
import com.clinicas.security.dto.rol.RolResponse;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.service.RolService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/roles")
public class RolController {
    private final RolService rolService;
    public RolController(RolService rolService) { this.rolService = rolService; }

    @GetMapping
    @PreAuthorize("hasAuthority('ROL_READ')")
    public List<RolResponse> listar() { return rolService.listar(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROL_READ')")
    public RolResponse obtener(@PathVariable Long id) { return rolService.obtener(id); }

    @PostMapping
    @PreAuthorize("hasAuthority('ROL_CREATE')")
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROL_UPDATE')")
    public RolResponse actualizar(@PathVariable Long id, @Valid @RequestBody RolRequest request) {
        return rolService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ROL_UPDATE')")
    public RolResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return rolService.cambiarEstado(id, request);
    }
}
