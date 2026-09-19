package com.clinicas.security.service;

import com.clinicas.security.dto.rolmodulo.RolModuloResponse;
import com.clinicas.security.entity.Modulo;
import com.clinicas.security.entity.Permiso;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.RolModulo;
import com.clinicas.security.entity.RolModuloPermiso;
import com.clinicas.security.exception.ResourceNotFoundException;
import com.clinicas.security.repository.ModuloRepository;
import com.clinicas.security.repository.PermisoRepository;
import com.clinicas.security.repository.RolModuloPermisoRepository;
import com.clinicas.security.repository.RolModuloRepository;
import com.clinicas.security.repository.RolRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolModuloService {

    private final RolRepository rolRepository;
    private final ModuloRepository moduloRepository;
    private final PermisoRepository permisoRepository;
    private final RolModuloRepository rolModuloRepository;
    private final RolModuloPermisoRepository rolModuloPermisoRepository;

    public RolModuloService(RolRepository rolRepository, ModuloRepository moduloRepository,
                            PermisoRepository permisoRepository, RolModuloRepository rolModuloRepository,
                            RolModuloPermisoRepository rolModuloPermisoRepository) {
        this.rolRepository = rolRepository;
        this.moduloRepository = moduloRepository;
        this.permisoRepository = permisoRepository;
        this.rolModuloRepository = rolModuloRepository;
        this.rolModuloPermisoRepository = rolModuloPermisoRepository;
    }

    @Transactional(readOnly = true)
    public List<RolModuloResponse> listarPorRol(Long idRol) {
        return rolModuloRepository.findByRolIdRol(idRol).stream().map(this::toResponse).toList();
    }

    @Transactional
    public RolModuloResponse asignarModulo(Long idRol, Long idModulo) {
        Rol rol = rol(idRol);
        Modulo modulo = modulo(idModulo);
        RolModulo rm = rolModuloRepository.findByRolIdRolAndModuloId(idRol, idModulo).orElseGet(() -> {
            RolModulo nuevo = new RolModulo();
            nuevo.setRol(rol);
            nuevo.setModulo(modulo);
            return nuevo;
        });
        rm.setActivo(true);
        return toResponse(rolModuloRepository.save(rm));
    }

    @Transactional
    public void revocarModulo(Long idRol, Long idModulo) {
        RolModulo rm = rolModuloRepository.findByRolIdRolAndModuloId(idRol, idModulo)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion rol-modulo no encontrada"));
        rm.setActivo(false);
    }

    @Transactional
    public RolModuloResponse asignarPermiso(Long idRolModulo, Long idPermiso) {
        RolModulo rolModulo = rolModulo(idRolModulo);
        Permiso permiso = permiso(idPermiso);
        RolModuloPermiso rmp = rolModuloPermisoRepository.findByRolModuloIdAndPermisoIdPermiso(idRolModulo, idPermiso)
                .orElseGet(() -> {
                    RolModuloPermiso nuevo = new RolModuloPermiso();
                    nuevo.setRolModulo(rolModulo);
                    nuevo.setPermiso(permiso);
                    return nuevo;
                });
        rmp.setActivo(true);
        rolModuloPermisoRepository.save(rmp);
        return toResponse(rolModuloRepository.findById(idRolModulo).orElseThrow());
    }

    @Transactional
    public void revocarPermiso(Long idRolModulo, Long idPermiso) {
        RolModuloPermiso rmp = rolModuloPermisoRepository.findByRolModuloIdAndPermisoIdPermiso(idRolModulo, idPermiso)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion rol-modulo-permiso no encontrada"));
        rmp.setActivo(false);
    }

    private Rol rol(Long id) {
        return rolRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
    }

    private Modulo modulo(Long id) {
        return moduloRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Modulo no encontrado: " + id));
    }

    private Permiso permiso(Long id) {
        return permisoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + id));
    }

    private RolModulo rolModulo(Long id) {
        return rolModuloRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("RolModulo no encontrado: " + id));
    }

    private RolModuloResponse toResponse(RolModulo rm) {
        List<String> permisos = rolModuloPermisoRepository.findByRolModuloId(rm.getId())
                .stream().filter(rmp -> Boolean.TRUE.equals(rmp.getActivo()))
                .map(rmp -> rmp.getPermiso().getCodigo()).toList();
        return new RolModuloResponse(
                rm.getId(),
                rm.getRol().getIdRol(), rm.getRol().getNombre(),
                rm.getModulo().getId(), rm.getModulo().getNombre(), rm.getModulo().getRuta(),
                rm.getActivo(), rm.getFechaCreacion(), permisos
        );
    }
}
