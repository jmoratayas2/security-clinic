package com.clinicas.security.dto.modulo;

import java.util.List;

public record MenuModuloResponse(
        Long idModulo,
        String nombre,
        String descripcion,
        String ruta,
        String icono,
        Integer orden,
        Long idModuloPadre,
        List<String> permisos,
        List<MenuModuloResponse> submodulos
) {}
