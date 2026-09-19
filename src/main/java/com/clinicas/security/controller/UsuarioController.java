package com.clinicas.security.controller;

import com.clinicas.security.dto.modulo.MenuModuloResponse;
import com.clinicas.security.dto.rol.AsignarRolRequest;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.dto.usuario.UsuarioRequest;
import com.clinicas.security.dto.usuario.UsuarioResponse;
import com.clinicas.security.service.ModuloService;
import com.clinicas.security.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/security/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final ModuloService moduloService;

    public UsuarioController(UsuarioService usuarioService, ModuloService moduloService) {
        this.usuarioService = usuarioService;
        this.moduloService = moduloService;
    }

    @GetMapping("/me/menu")
    public List<MenuModuloResponse> miMenu(Authentication authentication) {
        return moduloService.obtenerMenuPorUsername(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_READ')")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_READ')")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_CREATE')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_UPDATE')")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('USUARIO_UPDATE')")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return usuarioService.cambiarEstado(id, request);
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USUARIO_UPDATE')")
    public UsuarioResponse asignarRol(@PathVariable Long id, @Valid @RequestBody AsignarRolRequest request) {
        return usuarioService.asignarRol(id, request.rolId());
    }

    @PatchMapping("/{id}/roles/{rolId}/revocar")
    @PreAuthorize("hasAuthority('USUARIO_UPDATE')")
    public UsuarioResponse revocarRol(@PathVariable Long id, @PathVariable Long rolId) {
        return usuarioService.revocarRol(id, rolId);
    }
}
