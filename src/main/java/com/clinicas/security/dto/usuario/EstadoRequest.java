package com.clinicas.security.dto.usuario;

import jakarta.validation.constraints.NotNull;

public record EstadoRequest(@NotNull Boolean activo) {
}
