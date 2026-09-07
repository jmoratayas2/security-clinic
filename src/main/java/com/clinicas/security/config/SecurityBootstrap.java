package com.clinicas.security.config;

import com.clinicas.security.entity.Permiso;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.RolPermiso;
import com.clinicas.security.entity.Usuario;
import com.clinicas.security.entity.UsuarioRol;
import com.clinicas.security.repository.PermisoRepository;
import com.clinicas.security.repository.RolPermisoRepository;
import com.clinicas.security.repository.RolRepository;
import com.clinicas.security.repository.UsuarioRepository;
import com.clinicas.security.repository.UsuarioRolRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class SecurityBootstrap {
    private static final List<String> ROLES = List.of("ADMINISTRADOR", "RECEPCIONISTA", "MEDICO", "ENFERMERIA", "LABORATORIO", "AUDITOR");

    private static final List<String> PERMISOS = List.of(
            "CLINICA_READ", "CLINICA_CREATE", "CLINICA_UPDATE",
            "MEDICO_READ", "MEDICO_CREATE", "MEDICO_UPDATE",
            "ESPECIALIDAD_READ", "ESPECIALIDAD_CREATE", "ESPECIALIDAD_UPDATE",
            "PACIENTE_READ", "PACIENTE_CREATE", "PACIENTE_UPDATE",
            "CITA_READ", "CITA_CREATE", "CITA_UPDATE", "CITA_CANCEL",
            "CONSULTA_READ", "CONSULTA_CREATE",
            "DIAGNOSTICO_READ", "DIAGNOSTICO_CREATE",
            "TRATAMIENTO_READ", "TRATAMIENTO_CREATE", "TRATAMIENTO_UPDATE",
            "RECETA_READ", "RECETA_CREATE", "RECETA_ANULAR",
            "MEDICAMENTO_READ", "MEDICAMENTO_CREATE", "MEDICAMENTO_UPDATE",
            "EXAMEN_READ", "EXAMEN_CREATE", "EXAMEN_UPDATE", "RESULTADO_EXAMEN_READ", "RESULTADO_EXAMEN_CREATE",
            "USUARIO_READ", "USUARIO_CREATE", "USUARIO_UPDATE",
            "ROL_READ", "ROL_CREATE", "ROL_UPDATE",
            "PERMISO_READ", "PERMISO_CREATE", "PERMISO_UPDATE"
    );

    @Bean
    CommandLineRunner bootstrap(SecurityBootstrapRunner runner, @Value("${security.bootstrap.enabled:false}") boolean enabled) {
        return args -> {
            if (enabled) runner.run();
        };
    }

    @Configuration
    static class SecurityBootstrapRunner {
        private final UsuarioRepository usuarioRepository;
        private final RolRepository rolRepository;
        private final PermisoRepository permisoRepository;
        private final UsuarioRolRepository usuarioRolRepository;
        private final RolPermisoRepository rolPermisoRepository;
        private final PasswordEncoder passwordEncoder;

        SecurityBootstrapRunner(UsuarioRepository usuarioRepository, RolRepository rolRepository,
                                PermisoRepository permisoRepository, UsuarioRolRepository usuarioRolRepository,
                                RolPermisoRepository rolPermisoRepository, PasswordEncoder passwordEncoder) {
            this.usuarioRepository = usuarioRepository;
            this.rolRepository = rolRepository;
            this.permisoRepository = permisoRepository;
            this.usuarioRolRepository = usuarioRolRepository;
            this.rolPermisoRepository = rolPermisoRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        void run() {
            Map<String, Rol> roles = new LinkedHashMap<>();
            for (String nombre : ROLES) roles.put(nombre, ensureRol(nombre));

            Map<String, Permiso> permisos = new LinkedHashMap<>();
            for (String nombre : PERMISOS) permisos.put(nombre, ensurePermiso(nombre));

            Map<String, List<String>> matriz = matriz();
            for (Map.Entry<String, List<String>> entry : matriz.entrySet()) {
                Rol rol = roles.get(entry.getKey());
                for (String permiso : entry.getValue()) ensureRolPermiso(rol, permisos.get(permiso));
            }

            Usuario admin = usuarioRepository.findByUsername("admin").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("admin");
                u.setEmail("admin@clinicas.local");
                u.setPassword(passwordEncoder.encode("Admin123!"));
                u.setActivo(true);
                return usuarioRepository.save(u);
            });
            ensureUsuarioRol(admin, roles.get("ADMINISTRADOR"));

            Usuario recepcion = usuarioRepository.findByUsername("recepcion").orElseGet(() -> {
                Usuario u = new Usuario();
                u.setUsername("recepcion");
                u.setEmail("recepcion@clinicas.local");
                u.setPassword(passwordEncoder.encode("Recep123!"));
                u.setActivo(true);
                return usuarioRepository.save(u);
            });
            ensureUsuarioRol(recepcion, roles.get("RECEPCIONISTA"));
        }

        private Rol ensureRol(String nombre) {
            return rolRepository.findByNombre(nombre).orElseGet(() -> {
                Rol r = new Rol();
                r.setNombre(nombre);
                r.setDescripcion("Rol " + nombre);
                r.setActivo(true);
                return rolRepository.save(r);
            });
        }

        private Permiso ensurePermiso(String nombre) {
            return permisoRepository.findByNombre(nombre).orElseGet(() -> {
                Permiso p = new Permiso();
                p.setNombre(nombre);
                p.setDescripcion("Permiso " + nombre);
                p.setActivo(true);
                return permisoRepository.save(p);
            });
        }

        private void ensureRolPermiso(Rol rol, Permiso permiso) {
            RolPermiso rp = rolPermisoRepository.findByRolIdRolAndPermisoIdPermiso(rol.getIdRol(), permiso.getIdPermiso()).orElseGet(() -> {
                RolPermiso nuevo = new RolPermiso();
                nuevo.setRol(rol);
                nuevo.setPermiso(permiso);
                return nuevo;
            });
            rp.setActivo(true);
            rolPermisoRepository.save(rp);
        }

        private void ensureUsuarioRol(Usuario usuario, Rol rol) {
            UsuarioRol ur = usuarioRolRepository.findByUsuarioIdUsuarioAndRolIdRol(usuario.getIdUsuario(), rol.getIdRol()).orElseGet(() -> {
                UsuarioRol nuevo = new UsuarioRol();
                nuevo.setUsuario(usuario);
                nuevo.setRol(rol);
                return nuevo;
            });
            ur.setActivo(true);
            ur.setFechaAsignacion(LocalDateTime.now());
            ur.setFechaRevocacion(null);
            usuarioRolRepository.save(ur);
        }

        private Map<String, List<String>> matriz() {
            Map<String, List<String>> m = new LinkedHashMap<>();
            m.put("ADMINISTRADOR", PERMISOS);
            m.put("RECEPCIONISTA", List.of("PACIENTE_READ", "PACIENTE_CREATE", "PACIENTE_UPDATE", "CITA_READ", "CITA_CREATE", "CITA_UPDATE", "CITA_CANCEL", "MEDICO_READ", "CLINICA_READ", "ESPECIALIDAD_READ"));
            m.put("MEDICO", List.of("PACIENTE_READ", "CITA_READ", "CONSULTA_READ", "CONSULTA_CREATE", "DIAGNOSTICO_READ", "DIAGNOSTICO_CREATE", "TRATAMIENTO_READ", "TRATAMIENTO_CREATE", "TRATAMIENTO_UPDATE", "RECETA_READ", "RECETA_CREATE", "RECETA_ANULAR", "MEDICAMENTO_READ", "EXAMEN_READ", "EXAMEN_CREATE", "RESULTADO_EXAMEN_READ"));
            m.put("ENFERMERIA", List.of("PACIENTE_READ", "CITA_READ", "CONSULTA_READ"));
            m.put("LABORATORIO", List.of("PACIENTE_READ", "EXAMEN_READ", "EXAMEN_UPDATE", "RESULTADO_EXAMEN_READ", "RESULTADO_EXAMEN_CREATE"));
            m.put("AUDITOR", List.of("CLINICA_READ", "MEDICO_READ", "ESPECIALIDAD_READ", "PACIENTE_READ", "CITA_READ", "CONSULTA_READ", "DIAGNOSTICO_READ", "TRATAMIENTO_READ", "RECETA_READ", "MEDICAMENTO_READ", "EXAMEN_READ", "RESULTADO_EXAMEN_READ"));
            return m;
        }
    }
}
