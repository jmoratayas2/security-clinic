package com.clinicas.security.dto.rolmodulo;

import jakarta.validation.constraints.NotNull;

public record AsignarModuloRequest(@NotNull Long moduloId) {}
