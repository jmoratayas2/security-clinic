package com.clinicas.security.service;

import com.clinicas.security.dto.modulo.MenuModuloResponse;
import com.clinicas.security.dto.modulo.ModuloRequest;
import com.clinicas.security.dto.modulo.ModuloResponse;
import com.clinicas.security.entity.Modulo;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.RolModulo;
import com.clinicas.security.entity.RolModuloPermiso;
import com.clinicas.security.entity.Sistema;
import com.clinicas.security.entity.Usuario;
import com.clinicas.security.exception.ResourceNotFoundException;
import com.clinicas.security.repository.ModuloRepository;
import com.clinicas.security.repository.RolModuloPermisoRepository;
import com.clinicas.security.repository.RolModuloRepository;
import com.clinicas.security.repository.UsuarioRepository;
import com.clinicas.security.repository.UsuarioRolRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModuloService {

    private final ModuloRepository moduloRepository;
    private final SistemaService sistemaService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolModuloRepository rolModuloRepository;
    private final RolModuloPermisoRepository rolModuloPermisoRepository;

    public ModuloService(ModuloRepository moduloRepository,
                         SistemaService sistemaService,
                         UsuarioRepository usuarioRepository,
                         UsuarioRolRepository usuarioRolRepository,
                         RolModuloRepository rolModuloRepository,
                         RolModuloPermisoRepository rolModuloPermisoRepository) {
        this.moduloRepository = moduloRepository;
        this.sistemaService = sistemaService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolModuloRepository = rolModuloRepository;
        this.rolModuloPermisoRepository = rolModuloPermisoRepository;
    }

    @Transactional(readOnly = true)
    public List<ModuloResponse> listar() {
        return moduloRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ModuloResponse> listarPorSistema(Long idSistema) {
        return moduloRepository.findBySistemaId(idSistema).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ModuloResponse> listarRaicesPorSistema(Long idSistema) {
        return moduloRepository.findByModuloPadreIsNullAndSistemaId(idSistema).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ModuloResponse obtener(Long id) {
        return toResponse(modulo(id));
    }

    @Transactional(readOnly = true)
    public List<MenuModuloResponse> obtenerMenuPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return List.of();
        }

        List<Rol> rolesActivos = usuarioRolRepository.findRolesActivos(usuario.getIdUsuario());
        if (rolesActivos.isEmpty()) {
            return List.of();
        }

        Set<Long> rolIds = rolesActivos.stream().map(Rol::getIdRol).collect(Collectors.toSet());
        List<RolModulo> rolesModulos = rolModuloRepository.findAll().stream()
                .filter(rm -> rolIds.contains(rm.getRol().getIdRol()) && Boolean.TRUE.equals(rm.getActivo()) && Boolean.TRUE.equals(rm.getModulo().getActivo()))
                .toList();

        Map<Long, Set<String>> permisosPorModulo = new HashMap<>();
        for (RolModulo rm : rolesModulos) {
            List<RolModuloPermiso> rmps = rolModuloPermisoRepository.findByRolModuloId(rm.getId());
            for (RolModuloPermiso rmp : rmps) {
                if (Boolean.TRUE.equals(rmp.getActivo()) && Boolean.TRUE.equals(rmp.getPermiso().getActivo())) {
                    permisosPorModulo.computeIfAbsent(rm.getModulo().getId(), k -> new TreeSet<>())
                            .add(rmp.getPermiso().getCodigo());
                }
            }
        }

        Map<Long, Modulo> modulosAutorizados = new HashMap<>();
        for (RolModulo rm : rolesModulos) {
            modulosAutorizados.put(rm.getModulo().getId(), rm.getModulo());
        }

        // Incluir padres de módulos autorizados para mantener la jerarquía completa
        Set<Modulo> todosModulos = new HashSet<>(modulosAutorizados.values());
        for (Modulo m : modulosAutorizados.values()) {
            Modulo padre = m.getModuloPadre();
            while (padre != null && Boolean.TRUE.equals(padre.getActivo())) {
                todosModulos.add(padre);
                padre = padre.getModuloPadre();
            }
        }

        List<Modulo> raices = todosModulos.stream()
                .filter(m -> m.getModuloPadre() == null)
                .sorted(Comparator.comparing(Modulo::getOrden).thenComparing(Modulo::getNombre))
                .toList();

        return raices.stream().map(r -> buildMenuTree(r, todosModulos, permisosPorModulo)).toList();
    }

    private MenuModuloResponse buildMenuTree(Modulo m, Set<Modulo> todos, Map<Long, Set<String>> permisosPorModulo) {
        List<MenuModuloResponse> submodulos = todos.stream()
                .filter(sub -> sub.getModuloPadre() != null && sub.getModuloPadre().getId().equals(m.getId()))
                .sorted(Comparator.comparing(Modulo::getOrden).thenComparing(Modulo::getNombre))
                .map(sub -> buildMenuTree(sub, todos, permisosPorModulo))
                .toList();

        List<String> perms = permisosPorModulo.getOrDefault(m.getId(), Collections.emptySet()).stream().toList();
        Long idPadre = m.getModuloPadre() != null ? m.getModuloPadre().getId() : null;

        return new MenuModuloResponse(
                m.getId(),
                m.getNombre(),
                m.getDescripcion(),
                m.getRuta(),
                m.getIcono(),
                m.getOrden(),
                idPadre,
                perms,
                submodulos
        );
    }

    @Transactional
    public ModuloResponse crear(ModuloRequest request) {
        Sistema sistema = sistemaService.sistema(request.idSistema());
        Modulo m = new Modulo();
        m.setSistema(sistema);
        if (request.idModuloPadre() != null) m.setModuloPadre(modulo(request.idModuloPadre()));
        m.setNombre(request.nombre());
        m.setDescripcion(request.descripcion());
        m.setRuta(request.ruta());
        m.setIcono(request.icono());
        m.setOrden(request.orden() != null ? request.orden() : 0);
        m.setActivo(request.activo() == null ? true : request.activo());
        return toResponse(moduloRepository.save(m));
    }

    @Transactional
    public ModuloResponse actualizar(Long id, ModuloRequest request) {
        Modulo m = modulo(id);
        m.setSistema(sistemaService.sistema(request.idSistema()));
        m.setModuloPadre(request.idModuloPadre() != null ? modulo(request.idModuloPadre()) : null);
        m.setNombre(request.nombre());
        m.setDescripcion(request.descripcion());
        m.setRuta(request.ruta());
        m.setIcono(request.icono());
        if (request.orden() != null) m.setOrden(request.orden());
        if (request.activo() != null) m.setActivo(request.activo());
        return toResponse(m);
    }

    @Transactional
    public ModuloResponse cambiarEstado(Long id, boolean activo) {
        Modulo m = modulo(id);
        m.setActivo(activo);
        return toResponse(m);
    }

    public Modulo modulo(Long id) {
        return moduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modulo no encontrado: " + id));
    }

    private ModuloResponse toResponse(Modulo m) {
        Long idPadre = m.getModuloPadre() != null ? m.getModuloPadre().getId() : null;
        String nombrePadre = m.getModuloPadre() != null ? m.getModuloPadre().getNombre() : null;
        return new ModuloResponse(
                m.getId(),
                m.getSistema().getId(), m.getSistema().getNombre(),
                idPadre, nombrePadre,
                m.getNombre(), m.getDescripcion(), m.getRuta(), m.getIcono(),
                m.getOrden(), m.getActivo(), m.getFechaCreacion()
        );
    }
}
