package com.clinicas.security.dto.permiso;

import jakarta.validation.constraints.NotNull;

public record AsignarPermisoRequest(@NotNull Long permisoId) {
}
