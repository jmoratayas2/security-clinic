package com.clinicas.security.controller;

import com.clinicas.security.dto.permiso.AsignarPermisoRequest;
import com.clinicas.security.dto.rol.RolRequest;
import com.clinicas.security.dto.rol.RolResponse;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.service.RolService;
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
@RequestMapping("/api/security/roles")
public class RolController {
    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROL_READ')")
    public List<RolResponse> listar() {
        return rolService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROL_READ')")
    public RolResponse obtener(@PathVariable Long id) {
        return rolService.obtener(id);
    }

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

    @PostMapping("/{id}/permisos")
    @PreAuthorize("hasAuthority('ROL_UPDATE')")
    public RolResponse asignarPermiso(@PathVariable Long id, @Valid @RequestBody AsignarPermisoRequest request) {
        return rolService.asignarPermiso(id, request.permisoId());
    }

    @PatchMapping("/{id}/permisos/{permisoId}/revocar")
    @PreAuthorize("hasAuthority('ROL_UPDATE')")
    public RolResponse revocarPermiso(@PathVariable Long id, @PathVariable Long permisoId) {
        return rolService.revocarPermiso(id, permisoId);
    }
}
