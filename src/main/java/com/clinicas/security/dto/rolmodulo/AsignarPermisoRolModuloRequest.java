package com.clinicas.security.dto.rolmodulo;

import jakarta.validation.constraints.NotNull;

public record AsignarPermisoRolModuloRequest(@NotNull Long permisoId) {}
