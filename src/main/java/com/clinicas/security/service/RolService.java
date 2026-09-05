package com.clinicas.security.service;

import com.clinicas.security.dto.rol.RolRequest;
import com.clinicas.security.dto.rol.RolResponse;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.entity.Permiso;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.RolPermiso;
import com.clinicas.security.exception.BusinessRuleException;
import com.clinicas.security.exception.ResourceNotFoundException;
import com.clinicas.security.repository.PermisoRepository;
import com.clinicas.security.repository.RolPermisoRepository;
import com.clinicas.security.repository.RolRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolService {
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;

    public RolService(RolRepository rolRepository, PermisoRepository permisoRepository, RolPermisoRepository rolPermisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.rolPermisoRepository = rolPermisoRepository;
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RolResponse obtener(Long id) {
        return toResponse(rol(id));
    }

    @Transactional
    public RolResponse crear(RolRequest request) {
        if (rolRepository.existsByNombre(request.nombre())) throw new BusinessRuleException("Ya existe un rol con ese nombre");
        Rol r = new Rol();
        r.setNombre(request.nombre());
        r.setDescripcion(request.descripcion());
        r.setActivo(request.activo() == null ? true : request.activo());
        return toResponse(rolRepository.save(r));
    }

    @Transactional
    public RolResponse actualizar(Long id, RolRequest request) {
        if (rolRepository.existsByNombreAndIdRolNot(request.nombre(), id)) throw new BusinessRuleException("Ya existe un rol con ese nombre");
        Rol r = rol(id);
        r.setNombre(request.nombre());
        r.setDescripcion(request.descripcion());
        if (request.activo() != null) r.setActivo(request.activo());
        return toResponse(r);
    }

    @Transactional
    public RolResponse cambiarEstado(Long id, EstadoRequest request) {
        Rol r = rol(id);
        r.setActivo(request.activo());
        return toResponse(r);
    }

    @Transactional
    public RolResponse asignarPermiso(Long idRol, Long idPermiso) {
        Rol rol = rol(idRol);
        Permiso permiso = permiso(idPermiso);
        RolPermiso rp = rolPermisoRepository.findByRolIdRolAndPermisoIdPermiso(idRol, idPermiso).orElseGet(() -> {
            RolPermiso nuevo = new RolPermiso();
            nuevo.setRol(rol);
            nuevo.setPermiso(permiso);
            return nuevo;
        });
        rp.setActivo(true);
        rolPermisoRepository.save(rp);
        return toResponse(rol);
    }

    @Transactional
    public RolResponse revocarPermiso(Long idRol, Long idPermiso) {
        Rol rol = rol(idRol);
        RolPermiso rp = rolPermisoRepository.findByRolIdRolAndPermisoIdPermiso(idRol, idPermiso)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion rol-permiso no encontrada"));
        rp.setActivo(false);
        return toResponse(rol);
    }

    public Rol rol(Long id) {
        return rolRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
    }

    private Permiso permiso(Long id) {
        return permisoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + id));
    }

    private RolResponse toResponse(Rol rol) {
        return new RolResponse(rol.getIdRol(), rol.getNombre(), rol.getDescripcion(), rol.getActivo());
    }
}
