package com.clinicas.security.dto.modulo;

import java.time.LocalDateTime;

public record ModuloResponse(
        Long id,
        Long idSistema,
        String nombreSistema,
        Long idModuloPadre,
        String nombreModuloPadre,
        String nombre,
        String descripcion,
        String ruta,
        String icono,
        Integer orden,
        Boolean activo,
        LocalDateTime fechaCreacion
) {}
