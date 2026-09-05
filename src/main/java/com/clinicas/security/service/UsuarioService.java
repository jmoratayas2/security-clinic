package com.clinicas.security.service;

import com.clinicas.security.dto.usuario.EstadoRequest;
import com.clinicas.security.dto.usuario.UsuarioRequest;
import com.clinicas.security.dto.usuario.UsuarioResponse;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.Usuario;
import com.clinicas.security.entity.UsuarioRol;
import com.clinicas.security.exception.BusinessRuleException;
import com.clinicas.security.exception.ResourceNotFoundException;
import com.clinicas.security.repository.RolRepository;
import com.clinicas.security.repository.UsuarioRepository;
import com.clinicas.security.repository.UsuarioRolRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                          UsuarioRolRepository usuarioRolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return toResponse(usuario(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (request.password() == null || request.password().isBlank()) throw new BusinessRuleException("El password es obligatorio");
        if (usuarioRepository.existsByUsername(request.username())) throw new BusinessRuleException("Ya existe un usuario con ese username");
        if (request.email() != null && !request.email().isBlank() && usuarioRepository.existsByEmail(request.email())) throw new BusinessRuleException("Ya existe un usuario con ese email");
        Usuario u = new Usuario();
        u.setUsername(request.username());
        u.setPassword(passwordEncoder.encode(request.password()));
        u.setEmail(blankToNull(request.email()));
        u.setMedicoId(request.medicoId());
        u.setActivo(request.activo() == null ? true : request.activo());
        return toResponse(usuarioRepository.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario u = usuario(id);
        if (usuarioRepository.existsByUsernameAndIdUsuarioNot(request.username(), id)) throw new BusinessRuleException("Ya existe un usuario con ese username");
        if (request.email() != null && !request.email().isBlank() && usuarioRepository.existsByEmailAndIdUsuarioNot(request.email(), id)) throw new BusinessRuleException("Ya existe un usuario con ese email");
        u.setUsername(request.username());
        if (request.password() != null && !request.password().isBlank()) u.setPassword(passwordEncoder.encode(request.password()));
        u.setEmail(blankToNull(request.email()));
        u.setMedicoId(request.medicoId());
        if (request.activo() != null) u.setActivo(request.activo());
        return toResponse(u);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, EstadoRequest request) {
        Usuario u = usuario(id);
        u.setActivo(request.activo());
        return toResponse(u);
    }

    @Transactional
    public UsuarioResponse asignarRol(Long idUsuario, Long idRol) {
        Usuario usuario = usuario(idUsuario);
        Rol rol = rol(idRol);
        UsuarioRol usuarioRol = usuarioRolRepository.findByUsuarioIdUsuarioAndRolIdRol(idUsuario, idRol).orElseGet(() -> {
            UsuarioRol nuevo = new UsuarioRol();
            nuevo.setUsuario(usuario);
            nuevo.setRol(rol);
            return nuevo;
        });
        usuarioRol.setActivo(true);
        usuarioRol.setFechaAsignacion(LocalDateTime.now());
        usuarioRol.setFechaRevocacion(null);
        usuarioRolRepository.save(usuarioRol);
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse revocarRol(Long idUsuario, Long idRol) {
        Usuario usuario = usuario(idUsuario);
        UsuarioRol usuarioRol = usuarioRolRepository.findByUsuarioIdUsuarioAndRolIdRol(idUsuario, idRol)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion usuario-rol no encontrada"));
        usuarioRol.setActivo(false);
        usuarioRol.setFechaRevocacion(LocalDateTime.now());
        return toResponse(usuario);
    }

    public Usuario usuario(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }

    private Rol rol(Long id) {
        return rolRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        List<String> roles = usuarioRolRepository.findRolesActivos(usuario.getIdUsuario()).stream().map(Rol::getNombre).sorted().toList();
        return new UsuarioResponse(usuario.getIdUsuario(), usuario.getUsername(), usuario.getEmail(), usuario.getActivo(),
                usuario.getMedicoId(), usuario.getCreadoEn(), usuario.getActualizadoEn(), roles);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
