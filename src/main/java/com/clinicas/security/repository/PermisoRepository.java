package com.clinicas.security.repository;

import com.clinicas.security.entity.Permiso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    Optional<Permiso> findByNombre(String nombre);
    Optional<Permiso> findByCodigo(String codigo);
    boolean existsByNombre(String nombre);
    boolean existsByCodigo(String codigo);
    boolean existsByNombreAndIdPermisoNot(String nombre, Long idPermiso);
}

