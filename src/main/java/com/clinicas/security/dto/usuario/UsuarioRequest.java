package com.clinicas.security.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 80) String username,
        @Size(min = 8, max = 120) String password,
        @Email @Size(max = 160) String email,
        Long medicoId,
        Boolean activo
) {
}
