package com.clinicas.security.dto.rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RolRequest(@NotBlank @Size(max = 80) String nombre, @Size(max = 255) String descripcion, Boolean activo) {
}
