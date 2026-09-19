package com.clinicas.security.service;

import com.clinicas.security.dto.sistema.SistemaRequest;
import com.clinicas.security.dto.sistema.SistemaResponse;
import com.clinicas.security.entity.Sistema;
import com.clinicas.security.exception.BusinessRuleException;
import com.clinicas.security.exception.ResourceNotFoundException;
import com.clinicas.security.repository.SistemaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SistemaService {

    private final SistemaRepository sistemaRepository;

    public SistemaService(SistemaRepository sistemaRepository) {
        this.sistemaRepository = sistemaRepository;
    }

    @Transactional(readOnly = true)
    public List<SistemaResponse> listar() {
        return sistemaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SistemaResponse obtener(Long id) {
        return toResponse(sistema(id));
    }

    @Transactional
    public SistemaResponse crear(SistemaRequest request) {
        if (sistemaRepository.existsByNombre(request.nombre()))
            throw new BusinessRuleException("Ya existe un sistema con ese nombre");
        Sistema s = new Sistema();
        s.setNombre(request.nombre());
        s.setDominio(request.dominio());
        s.setActivo(request.activo() == null ? true : request.activo());
        return toResponse(sistemaRepository.save(s));
    }

    @Transactional
    public SistemaResponse actualizar(Long id, SistemaRequest request) {
        Sistema s = sistema(id);
        s.setNombre(request.nombre());
        s.setDominio(request.dominio());
        if (request.activo() != null) s.setActivo(request.activo());
        return toResponse(s);
    }

    @Transactional
    public SistemaResponse cambiarEstado(Long id, boolean activo) {
        Sistema s = sistema(id);
        s.setActivo(activo);
        return toResponse(s);
    }

    public Sistema sistema(Long id) {
        return sistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sistema no encontrado: " + id));
    }

    private SistemaResponse toResponse(Sistema s) {
        return new SistemaResponse(s.getId(), s.getNombre(), s.getDominio(), s.getActivo(), s.getFechaCreacion());
    }
}
