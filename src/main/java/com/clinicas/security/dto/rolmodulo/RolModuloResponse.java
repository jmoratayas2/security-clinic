package com.clinicas.security.dto.rolmodulo;

import java.time.LocalDateTime;
import java.util.List;

public record RolModuloResponse(
        Long id,
        Long idRol,
        String nombreRol,
        Long idModulo,
        String nombreModulo,
        String rutaModulo,
        Boolean activo,
        LocalDateTime fechaCreacion,
        List<String> permisos
) {}
