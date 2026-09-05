package com.clinicas.security.service;

import com.clinicas.security.dto.permiso.PermisoRequest;
import com.clinicas.security.dto.permiso.PermisoResponse;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.entity.Permiso;
import com.clinicas.security.exception.BusinessRuleException;
import com.clinicas.security.exception.ResourceNotFoundException;
import com.clinicas.security.repository.PermisoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermisoService {
    private final PermisoRepository permisoRepository;

    public PermisoService(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> listar() {
        return permisoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PermisoResponse obtener(Long id) {
        return toResponse(permiso(id));
    }

    @Transactional
    public PermisoResponse crear(PermisoRequest request) {
        if (permisoRepository.existsByNombre(request.nombre())) throw new BusinessRuleException("Ya existe un permiso con ese nombre");
        Permiso p = new Permiso();
        p.setNombre(request.nombre());
        p.setDescripcion(request.descripcion());
        p.setActivo(request.activo() == null ? true : request.activo());
        return toResponse(permisoRepository.save(p));
    }

    @Transactional
    public PermisoResponse actualizar(Long id, PermisoRequest request) {
        if (permisoRepository.existsByNombreAndIdPermisoNot(request.nombre(), id)) throw new BusinessRuleException("Ya existe un permiso con ese nombre");
        Permiso p = permiso(id);
        p.setNombre(request.nombre());
        p.setDescripcion(request.descripcion());
        if (request.activo() != null) p.setActivo(request.activo());
        return toResponse(p);
    }

    @Transactional
    public PermisoResponse cambiarEstado(Long id, EstadoRequest request) {
        Permiso p = permiso(id);
        p.setActivo(request.activo());
        return toResponse(p);
    }

    public Permiso permiso(Long id) {
        return permisoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado: " + id));
    }

    private PermisoResponse toResponse(Permiso permiso) {
        return new PermisoResponse(permiso.getIdPermiso(), permiso.getNombre(), permiso.getDescripcion(), permiso.getActivo());
    }
}
