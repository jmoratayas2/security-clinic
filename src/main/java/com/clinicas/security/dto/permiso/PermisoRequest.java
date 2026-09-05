package com.clinicas.security.dto.permiso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermisoRequest(@NotBlank @Size(max = 100) String nombre, @Size(max = 255) String descripcion, Boolean activo) {
}
