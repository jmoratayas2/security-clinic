package com.clinicas.security.dto.usuario;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponse(Long idUsuario, String username, String email, Boolean activo, Long medicoId,
                              LocalDateTime creadoEn, LocalDateTime actualizadoEn, List<String> roles) {
}
