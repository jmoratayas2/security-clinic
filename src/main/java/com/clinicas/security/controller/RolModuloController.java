package com.clinicas.security.controller;

import com.clinicas.security.dto.rolmodulo.AsignarModuloRequest;
import com.clinicas.security.dto.rolmodulo.AsignarPermisoRolModuloRequest;
import com.clinicas.security.dto.rolmodulo.RolModuloResponse;
import com.clinicas.security.service.RolModuloService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security")
public class RolModuloController {
    private final RolModuloService rolModuloService;
    public RolModuloController(RolModuloService rolModuloService) { this.rolModuloService = rolModuloService; }

    @GetMapping("/roles/{idRol}/modulos")
    @PreAuthorize("hasAuthority('ROL_READ')")
    public List<RolModuloResponse> listarModulosDeRol(@PathVariable Long idRol) {
        return rolModuloService.listarPorRol(idRol);
    }

    @PostMapping("/roles/{idRol}/modulos")
    @PreAuthorize("hasAuthority('ROL_UPDATE')")
    public ResponseEntity<RolModuloResponse> asignarModulo(@PathVariable Long idRol,
            @Valid @RequestBody AsignarModuloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolModuloService.asignarModulo(idRol, request.moduloId()));
    }

    @DeleteMapping("/roles/{idRol}/modulos/{idModulo}")
    @PreAuthorize("hasAuthority('ROL_UPDATE')")
    public ResponseEntity<Void> revocarModulo(@PathVariable Long idRol, @PathVariable Long idModulo) {
        rolModuloService.revocarModulo(idRol, idModulo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rol-modulo/{idRolModulo}/permisos")
    @PreAuthorize("hasAuthority('ROL_UPDATE')")
    public ResponseEntity<RolModuloResponse> asignarPermiso(@PathVariable Long idRolModulo,
            @Valid @RequestBody AsignarPermisoRolModuloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolModuloService.asignarPermiso(idRolModulo, request.permisoId()));
    }

    @DeleteMapping("/rol-modulo/{idRolModulo}/permisos/{idPermiso}")
    @PreAuthorize("hasAuthority('ROL_UPDATE')")
    public ResponseEntity<Void> revocarPermiso(@PathVariable Long idRolModulo, @PathVariable Long idPermiso) {
        rolModuloService.revocarPermiso(idRolModulo, idPermiso);
        return ResponseEntity.noContent().build();
    }
}
