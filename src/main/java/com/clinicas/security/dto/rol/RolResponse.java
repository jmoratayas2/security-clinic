package com.clinicas.security.dto.rol;

import java.time.LocalDateTime;

public record RolResponse(
        Long idRol,
        Long idSistema,
        String nombreSistema,
        String nombre,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion
) {}

