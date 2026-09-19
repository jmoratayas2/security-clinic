package com.clinicas.security.repository;

import com.clinicas.security.entity.RolModulo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolModuloRepository extends JpaRepository<RolModulo, Long> {
    Optional<RolModulo> findByRolIdRolAndModuloId(Long idRol, Long idModulo);
    List<RolModulo> findByRolIdRol(Long idRol);
    List<RolModulo> findByRolIdRolAndActivoTrue(Long idRol);
}
