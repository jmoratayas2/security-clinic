package com.clinicas.security.controller;

import com.clinicas.security.dto.modulo.MenuModuloResponse;
import com.clinicas.security.dto.modulo.ModuloRequest;
import com.clinicas.security.dto.modulo.ModuloResponse;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.service.ModuloService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/modulos")
public class ModuloController {
    private final ModuloService moduloService;
    public ModuloController(ModuloService moduloService) { this.moduloService = moduloService; }

    @GetMapping("/menu")
    public List<MenuModuloResponse> obtenerMenu(Authentication authentication) {
        return moduloService.obtenerMenuPorUsername(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MODULO_READ')")
    public List<ModuloResponse> listar() { return moduloService.listar(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MODULO_READ')")
    public ModuloResponse obtener(@PathVariable Long id) { return moduloService.obtener(id); }

    @PostMapping
    @PreAuthorize("hasAuthority('MODULO_CREATE')")
    public ResponseEntity<ModuloResponse> crear(@Valid @RequestBody ModuloRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduloService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MODULO_UPDATE')")
    public ModuloResponse actualizar(@PathVariable Long id, @Valid @RequestBody ModuloRequest request) {
        return moduloService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('MODULO_UPDATE')")
    public ModuloResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
        return moduloService.cambiarEstado(id, request.activo());
    }
}
