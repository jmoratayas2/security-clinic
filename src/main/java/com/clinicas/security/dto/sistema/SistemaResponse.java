package com.clinicas.security.dto.sistema;

import java.time.LocalDateTime;

public record SistemaResponse(
        Long id,
        String nombre,
        String dominio,
        Boolean activo,
        LocalDateTime fechaCreacion
) {}
