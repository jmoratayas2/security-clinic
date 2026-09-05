package com.clinicas.security.dto.rol;

import jakarta.validation.constraints.NotNull;

public record AsignarRolRequest(@NotNull Long rolId) {
}
