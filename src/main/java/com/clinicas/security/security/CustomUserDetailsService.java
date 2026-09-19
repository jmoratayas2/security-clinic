package com.clinicas.security.security;

import com.clinicas.security.entity.Permiso;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.Usuario;
import com.clinicas.security.repository.RolModuloPermisoRepository;
import com.clinicas.security.repository.UsuarioRepository;
import com.clinicas.security.repository.UsuarioRolRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolModuloPermisoRepository rolModuloPermisoRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository,
                                    UsuarioRolRepository usuarioRolRepository,
                                    RolModuloPermisoRepository rolModuloPermisoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolModuloPermisoRepository = rolModuloPermisoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) throw new DisabledException("Usuario inactivo");

        List<Rol> roles = usuarioRolRepository.findRolesActivos(usuario.getIdUsuario());
        List<Long> roleIds = roles.stream().map(Rol::getIdRol).toList();
        // Carga permisos via: rol → rol_modulo → rol_modulo_permiso → permiso
        List<Permiso> permisos = roleIds.isEmpty()
                ? List.of()
                : rolModuloPermisoRepository.findPermisosActivosPorRoles(roleIds);

        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        roles.stream().map(r -> "ROLE_" + r.getNombre()).map(SimpleGrantedAuthority::new).forEach(authorities::add);
        permisos.stream().map(Permiso::getCodigo).map(SimpleGrantedAuthority::new).forEach(authorities::add);

        return new SecurityUser(usuario.getIdUsuario(), usuario.getMedicoId(), usuario.getUsername(), usuario.getPassword(),
                Boolean.TRUE.equals(usuario.getActivo()), authorities);
    }
}
