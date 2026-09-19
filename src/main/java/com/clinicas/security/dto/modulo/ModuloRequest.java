package com.clinicas.security.dto.modulo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModuloRequest(
        @NotNull Long idSistema,
        Long idModuloPadre,
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 255) String descripcion,
        @Size(max = 255) String ruta,
        @Size(max = 100) String icono,
        Integer orden,
        Boolean activo
) {}
