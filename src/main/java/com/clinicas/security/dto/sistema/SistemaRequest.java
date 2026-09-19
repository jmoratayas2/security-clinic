package com.clinicas.security.dto.sistema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SistemaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 100) String dominio,
        Boolean activo
) {}
