package com.clinicas.security.repository;

import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.UsuarioRol;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {
    Optional<UsuarioRol> findByUsuarioIdUsuarioAndRolIdRol(Long idUsuario, Long idRol);

    @Query("""
            select ur.rol
            from UsuarioRol ur
            where ur.usuario.idUsuario = :usuarioId
              and ur.activo = true
              and ur.rol.activo = true
            """)
    List<Rol> findRolesActivos(@Param("usuarioId") Long usuarioId);

    List<UsuarioRol> findByUsuarioIdUsuario(Long idUsuario);
}
