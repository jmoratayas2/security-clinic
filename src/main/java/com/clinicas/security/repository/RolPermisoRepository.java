package com.clinicas.security.repository;

import com.clinicas.security.entity.Permiso;
import com.clinicas.security.entity.RolPermiso;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, Long> {
    Optional<RolPermiso> findByRolIdRolAndPermisoIdPermiso(Long idRol, Long idPermiso);

    @Query("""
            select distinct rp.permiso
            from RolPermiso rp
            where rp.rol.idRol in :rolIds
              and rp.activo = true
              and rp.rol.activo = true
              and rp.permiso.activo = true
            """)
    List<Permiso> findPermisosActivosPorRoles(@Param("rolIds") Collection<Long> rolIds);

    List<RolPermiso> findByRolIdRol(Long idRol);
}
