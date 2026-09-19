package com.clinicas.security.repository;

import com.clinicas.security.entity.Permiso;
import com.clinicas.security.entity.RolModuloPermiso;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolModuloPermisoRepository extends JpaRepository<RolModuloPermiso, Long> {
    Optional<RolModuloPermiso> findByRolModuloIdAndPermisoIdPermiso(Long idRolModulo, Long idPermiso);
    List<RolModuloPermiso> findByRolModuloId(Long idRolModulo);

    @Query("""
            select distinct rmp.permiso
            from RolModuloPermiso rmp
            where rmp.rolModulo.rol.idRol in :rolIds
              and rmp.activo = true
              and rmp.rolModulo.activo = true
              and rmp.rolModulo.rol.activo = true
              and rmp.permiso.activo = true
            """)
    List<Permiso> findPermisosActivosPorRoles(@Param("rolIds") Collection<Long> rolIds);
}
