package com.clinicas.security.dto.permiso;

import java.time.LocalDateTime;

public record PermisoResponse(
        Long idPermiso,
        String nombre,
        String codigo,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion
) {}

