package com.clinicas.security.dto.rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RolRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 255) String descripcion,
        Long idSistema,
        Boolean activo
) {}
