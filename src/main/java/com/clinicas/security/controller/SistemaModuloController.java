package com.clinicas.security.controller;

import com.clinicas.security.dto.modulo.ModuloResponse;
import com.clinicas.security.service.ModuloService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/sistemas/{idSistema}/modulos")
public class SistemaModuloController {
    private final ModuloService moduloService;
    public SistemaModuloController(ModuloService moduloService) { this.moduloService = moduloService; }

    @GetMapping
    @PreAuthorize("hasAuthority('MODULO_READ')")
    public List<ModuloResponse> listarPorSistema(@PathVariable Long idSistema) {
        return moduloService.listarPorSistema(idSistema);
    }
}
