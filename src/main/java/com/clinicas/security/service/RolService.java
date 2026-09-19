package com.clinicas.security.service;

import com.clinicas.security.dto.rol.RolRequest;
import com.clinicas.security.dto.rol.RolResponse;
import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.Sistema;
import com.clinicas.security.exception.BusinessRuleException;
import com.clinicas.security.exception.ResourceNotFoundException;
import com.clinicas.security.repository.RolRepository;
import com.clinicas.security.repository.SistemaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RolService {
    private final RolRepository rolRepository;
    private final SistemaRepository sistemaRepository;

    public RolService(RolRepository rolRepository, SistemaRepository sistemaRepository) {
        this.rolRepository = rolRepository;
        this.sistemaRepository = sistemaRepository;
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
        if (rolRepository.existsByNombre(request.nombre()))
            throw new BusinessRuleException("Ya existe un rol con ese nombre");
        Rol r = new Rol();
        r.setNombre(request.nombre());
        r.setDescripcion(request.descripcion());
        r.setActivo(request.activo() == null ? true : request.activo());
        if (request.idSistema() != null) r.setSistema(sistema(request.idSistema()));
        return toResponse(rolRepository.save(r));
    }

    @Transactional
    public RolResponse actualizar(Long id, RolRequest request) {
        if (rolRepository.existsByNombreAndIdRolNot(request.nombre(), id))
            throw new BusinessRuleException("Ya existe un rol con ese nombre");
        Rol r = rol(id);
        r.setNombre(request.nombre());
        r.setDescripcion(request.descripcion());
        if (request.activo() != null) r.setActivo(request.activo());
        r.setSistema(request.idSistema() != null ? sistema(request.idSistema()) : null);
        return toResponse(r);
    }

    @Transactional
    public RolResponse cambiarEstado(Long id, EstadoRequest request) {
        Rol r = rol(id);
        r.setActivo(request.activo());
        return toResponse(r);
    }

    public Rol rol(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
    }

    private Sistema sistema(Long id) {
        return sistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sistema no encontrado: " + id));
    }

    private RolResponse toResponse(Rol rol) {
        Long idSistema = rol.getSistema() != null ? rol.getSistema().getId() : null;
        String nombreSistema = rol.getSistema() != null ? rol.getSistema().getNombre() : null;
        return new RolResponse(
                rol.getIdRol(), idSistema, nombreSistema,
                rol.getNombre(), rol.getDescripcion(), rol.getActivo(), rol.getFechaCreacion()
        );
    }
}
