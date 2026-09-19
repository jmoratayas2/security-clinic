package com.clinicas.security.config;

import com.clinicas.security.entity.Modulo;
import com.clinicas.security.entity.Permiso;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.RolModulo;
import com.clinicas.security.entity.RolModuloPermiso;
import com.clinicas.security.entity.Sistema;
import com.clinicas.security.entity.Usuario;
import com.clinicas.security.entity.UsuarioRol;
import com.clinicas.security.repository.ModuloRepository;
import com.clinicas.security.repository.PermisoRepository;
import com.clinicas.security.repository.RolModuloPermisoRepository;
import com.clinicas.security.repository.RolModuloRepository;
import com.clinicas.security.repository.RolRepository;
import com.clinicas.security.repository.SistemaRepository;
import com.clinicas.security.repository.UsuarioRepository;
import com.clinicas.security.repository.UsuarioRolRepository;
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

    private static final String SISTEMA_NOMBRE = "CLINICA_SYSTEM";
    private static final String SISTEMA_DOMINIO = "clinicas.local";

    private static final List<String> ROLES = List.of(
            "ADMINISTRADOR", "RECEPCIONISTA", "MEDICO", "ENFERMERIA", "LABORATORIO", "AUDITOR"
    );

    private static final List<String[]> PERMISOS = List.of(
            new String[]{"Sistema - Leer", "SISTEMA_READ"},
            new String[]{"Sistema - Crear", "SISTEMA_CREATE"},
            new String[]{"Sistema - Actualizar", "SISTEMA_UPDATE"},
            new String[]{"Modulo - Leer", "MODULO_READ"},
            new String[]{"Modulo - Crear", "MODULO_CREATE"},
            new String[]{"Modulo - Actualizar", "MODULO_UPDATE"},
            new String[]{"Clinica - Leer", "CLINICA_READ"},
            new String[]{"Clinica - Crear", "CLINICA_CREATE"},
            new String[]{"Clinica - Actualizar", "CLINICA_UPDATE"},
            new String[]{"Medico - Leer", "MEDICO_READ"},
            new String[]{"Medico - Crear", "MEDICO_CREATE"},
            new String[]{"Medico - Actualizar", "MEDICO_UPDATE"},
            new String[]{"Especialidad - Leer", "ESPECIALIDAD_READ"},
            new String[]{"Especialidad - Crear", "ESPECIALIDAD_CREATE"},
            new String[]{"Especialidad - Actualizar", "ESPECIALIDAD_UPDATE"},
            new String[]{"Paciente - Leer", "PACIENTE_READ"},
            new String[]{"Paciente - Crear", "PACIENTE_CREATE"},
            new String[]{"Paciente - Actualizar", "PACIENTE_UPDATE"},
            new String[]{"Cita - Leer", "CITA_READ"},
            new String[]{"Cita - Crear", "CITA_CREATE"},
            new String[]{"Cita - Actualizar", "CITA_UPDATE"},
            new String[]{"Cita - Cancelar", "CITA_CANCEL"},
            new String[]{"Consulta - Leer", "CONSULTA_READ"},
            new String[]{"Consulta - Crear", "CONSULTA_CREATE"},
            new String[]{"Diagnostico - Leer", "DIAGNOSTICO_READ"},
            new String[]{"Diagnostico - Crear", "DIAGNOSTICO_CREATE"},
            new String[]{"Tratamiento - Leer", "TRATAMIENTO_READ"},
            new String[]{"Tratamiento - Crear", "TRATAMIENTO_CREATE"},
            new String[]{"Tratamiento - Actualizar", "TRATAMIENTO_UPDATE"},
            new String[]{"Receta - Leer", "RECETA_READ"},
            new String[]{"Receta - Crear", "RECETA_CREATE"},
            new String[]{"Receta - Anular", "RECETA_ANULAR"},
            new String[]{"Medicamento - Leer", "MEDICAMENTO_READ"},
            new String[]{"Medicamento - Crear", "MEDICAMENTO_CREATE"},
            new String[]{"Medicamento - Actualizar", "MEDICAMENTO_UPDATE"},
            new String[]{"Examen - Leer", "EXAMEN_READ"},
            new String[]{"Examen - Crear", "EXAMEN_CREATE"},
            new String[]{"Examen - Actualizar", "EXAMEN_UPDATE"},
            new String[]{"Resultado Examen - Leer", "RESULTADO_EXAMEN_READ"},
            new String[]{"Resultado Examen - Crear", "RESULTADO_EXAMEN_CREATE"},
            new String[]{"Usuario - Leer", "USUARIO_READ"},
            new String[]{"Usuario - Crear", "USUARIO_CREATE"},
            new String[]{"Usuario - Actualizar", "USUARIO_UPDATE"},
            new String[]{"Rol - Leer", "ROL_READ"},
            new String[]{"Rol - Crear", "ROL_CREATE"},
            new String[]{"Rol - Actualizar", "ROL_UPDATE"},
            new String[]{"Permiso - Leer", "PERMISO_READ"},
            new String[]{"Permiso - Crear", "PERMISO_CREATE"},
            new String[]{"Permiso - Actualizar", "PERMISO_UPDATE"}
    );

    // nombre, ruta, icono Material, orden, nombreModuloPadre (null si es raiz)
    private static final List<String[]> MODULOS = List.of(
            new String[]{"Seguridad", "/seguridad", "security", "1", null},
            new String[]{"Usuarios", "/seguridad/usuarios", "manage_accounts", "1", "Seguridad"},
            new String[]{"Roles", "/seguridad/roles", "badge", "2", "Seguridad"},
            new String[]{"Clinicas", "/clinicas", "local_hospital", "2", null},
            new String[]{"Medicos", "/medicos", "medical_services", "3", null},
            new String[]{"Especialidades", "/especialidades", "school", "1", "Medicos"},
            new String[]{"Pacientes", "/pacientes", "people", "4", null},
            new String[]{"Citas", "/citas", "event", "5", null},
            new String[]{"Consultas", "/consultas", "assignment", "6", null},
            new String[]{"Examenes", "/examenes", "science", "7", null},
            new String[]{"Medicamentos", "/medicamentos", "medication", "8", null},
            new String[]{"Reportes", "/reportes", "bar_chart", "9", null}
    );

    private static Map<String, List<String>> moduloPermisos() {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("Seguridad", List.of("SISTEMA_READ","SISTEMA_CREATE","SISTEMA_UPDATE","MODULO_READ","MODULO_CREATE","MODULO_UPDATE"));
        m.put("Usuarios", List.of("USUARIO_READ","USUARIO_CREATE","USUARIO_UPDATE"));
        m.put("Roles", List.of("ROL_READ","ROL_CREATE","ROL_UPDATE","PERMISO_READ","PERMISO_CREATE","PERMISO_UPDATE"));
        m.put("Clinicas", List.of("CLINICA_READ","CLINICA_CREATE","CLINICA_UPDATE"));
        m.put("Medicos", List.of("MEDICO_READ","MEDICO_CREATE","MEDICO_UPDATE"));
        m.put("Especialidades", List.of("ESPECIALIDAD_READ","ESPECIALIDAD_CREATE","ESPECIALIDAD_UPDATE"));
        m.put("Pacientes", List.of("PACIENTE_READ","PACIENTE_CREATE","PACIENTE_UPDATE"));
        m.put("Citas", List.of("CITA_READ","CITA_CREATE","CITA_UPDATE","CITA_CANCEL"));
        m.put("Consultas", List.of("CONSULTA_READ","CONSULTA_CREATE","DIAGNOSTICO_READ","DIAGNOSTICO_CREATE","TRATAMIENTO_READ","TRATAMIENTO_CREATE","TRATAMIENTO_UPDATE","RECETA_READ","RECETA_CREATE","RECETA_ANULAR"));
        m.put("Examenes", List.of("EXAMEN_READ","EXAMEN_CREATE","EXAMEN_UPDATE","RESULTADO_EXAMEN_READ","RESULTADO_EXAMEN_CREATE"));
        m.put("Medicamentos", List.of("MEDICAMENTO_READ","MEDICAMENTO_CREATE","MEDICAMENTO_UPDATE"));
        m.put("Reportes", List.of("CLINICA_READ","MEDICO_READ","ESPECIALIDAD_READ","PACIENTE_READ","CITA_READ","CONSULTA_READ","DIAGNOSTICO_READ","TRATAMIENTO_READ","RECETA_READ","MEDICAMENTO_READ","EXAMEN_READ","RESULTADO_EXAMEN_READ"));
        return m;
    }

    private static Map<String, Map<String, List<String>>> matrizRolModuloPermiso() {
        Map<String, Map<String, List<String>>> m = new LinkedHashMap<>();
        m.put("ADMINISTRADOR", moduloPermisos());
        Map<String, List<String>> recep = new LinkedHashMap<>();
        recep.put("Clinicas", List.of("CLINICA_READ"));
        recep.put("Medicos", List.of("MEDICO_READ"));
        recep.put("Especialidades", List.of("ESPECIALIDAD_READ"));
        recep.put("Pacientes", List.of("PACIENTE_READ","PACIENTE_CREATE","PACIENTE_UPDATE"));
        recep.put("Citas", List.of("CITA_READ","CITA_CREATE","CITA_UPDATE","CITA_CANCEL"));
        m.put("RECEPCIONISTA", recep);
        Map<String, List<String>> medico = new LinkedHashMap<>();
        medico.put("Pacientes", List.of("PACIENTE_READ"));
        medico.put("Citas", List.of("CITA_READ"));
        medico.put("Consultas", List.of("CONSULTA_READ","CONSULTA_CREATE","DIAGNOSTICO_READ","DIAGNOSTICO_CREATE","TRATAMIENTO_READ","TRATAMIENTO_CREATE","TRATAMIENTO_UPDATE","RECETA_READ","RECETA_CREATE","RECETA_ANULAR"));
        medico.put("Examenes", List.of("EXAMEN_READ","EXAMEN_CREATE","RESULTADO_EXAMEN_READ"));
        medico.put("Medicamentos", List.of("MEDICAMENTO_READ"));
        m.put("MEDICO", medico);
        Map<String, List<String>> enfermeria = new LinkedHashMap<>();
        enfermeria.put("Pacientes", List.of("PACIENTE_READ"));
        enfermeria.put("Citas", List.of("CITA_READ"));
        enfermeria.put("Consultas", List.of("CONSULTA_READ"));
        m.put("ENFERMERIA", enfermeria);
        Map<String, List<String>> lab = new LinkedHashMap<>();
        lab.put("Pacientes", List.of("PACIENTE_READ"));
        lab.put("Examenes", List.of("EXAMEN_READ","EXAMEN_UPDATE","RESULTADO_EXAMEN_READ","RESULTADO_EXAMEN_CREATE"));
        m.put("LABORATORIO", lab);
        Map<String, List<String>> auditor = new LinkedHashMap<>();
        auditor.put("Clinicas", List.of("CLINICA_READ"));
        auditor.put("Medicos", List.of("MEDICO_READ"));
        auditor.put("Especialidades", List.of("ESPECIALIDAD_READ"));
        auditor.put("Pacientes", List.of("PACIENTE_READ"));
        m.put("AUDITOR", auditor);
        return m;
    }

    @Bean
    CommandLineRunner bootstrap(SecurityBootstrapRunner runner,
                                @Value("${security.bootstrap.enabled:false}") boolean enabled) {
        return args -> { if (enabled) runner.run(); };
    }

    @Configuration
    static class SecurityBootstrapRunner {
        private final SistemaRepository sistemaRepository;
        private final RolRepository rolRepository;
        private final PermisoRepository permisoRepository;
        private final ModuloRepository moduloRepository;
        private final RolModuloRepository rolModuloRepository;
        private final RolModuloPermisoRepository rolModuloPermisoRepository;
        private final UsuarioRepository usuarioRepository;
        private final UsuarioRolRepository usuarioRolRepository;
        private final PasswordEncoder passwordEncoder;

        SecurityBootstrapRunner(SistemaRepository sistemaRepository, RolRepository rolRepository,
                                PermisoRepository permisoRepository, ModuloRepository moduloRepository,
                                RolModuloRepository rolModuloRepository,
                                RolModuloPermisoRepository rolModuloPermisoRepository,
                                UsuarioRepository usuarioRepository, UsuarioRolRepository usuarioRolRepository,
                                PasswordEncoder passwordEncoder) {
            this.sistemaRepository = sistemaRepository;
            this.rolRepository = rolRepository;
            this.permisoRepository = permisoRepository;
            this.moduloRepository = moduloRepository;
            this.rolModuloRepository = rolModuloRepository;
            this.rolModuloPermisoRepository = rolModuloPermisoRepository;
            this.usuarioRepository = usuarioRepository;
            this.usuarioRolRepository = usuarioRolRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        void run() {
            Sistema sistema = ensureSistema(SISTEMA_NOMBRE, SISTEMA_DOMINIO);
            Map<String, Rol> roles = new LinkedHashMap<>();
            for (String nombre : ROLES) roles.put(nombre, ensureRol(nombre, sistema));
            Map<String, Permiso> permisos = new LinkedHashMap<>();
            for (String[] p : PERMISOS) permisos.put(p[1], ensurePermiso(p[0], p[1]));

            // Primero crear modulos raiz (sin padre)
            Map<String, Modulo> modulos = new LinkedHashMap<>();
            for (String[] m : MODULOS) {
                if (m[4] == null) {
                    modulos.put(m[0], ensureModulo(sistema, m[0], m[1], m[2], Integer.parseInt(m[3]), null));
                }
            }
            // Luego crear modulos hijos con referencia a su modulo padre
            for (String[] m : MODULOS) {
                if (m[4] != null) {
                    Modulo padre = modulos.get(m[4]);
                    modulos.put(m[0], ensureModulo(sistema, m[0], m[1], m[2], Integer.parseInt(m[3]), padre));
                }
            }

            Map<String, Map<String, List<String>>> matriz = matrizRolModuloPermiso();
            for (Map.Entry<String, Map<String, List<String>>> rolEntry : matriz.entrySet()) {
                Rol rol = roles.get(rolEntry.getKey());
                if (rol == null) continue;
                for (Map.Entry<String, List<String>> moduloEntry : rolEntry.getValue().entrySet()) {
                    Modulo modulo = modulos.get(moduloEntry.getKey());
                    if (modulo == null) continue;
                    RolModulo rolModulo = ensureRolModulo(rol, modulo);
                    for (String cod : moduloEntry.getValue()) {
                        Permiso permiso = permisos.get(cod);
                        if (permiso != null) ensureRolModuloPermiso(rolModulo, permiso);
                    }
                }
            }

            Usuario admin = ensureUsuario("admin", "admin@clinicas.local", "Admin123!", "Admin", "General");
            ensureUsuarioRol(admin, roles.get("ADMINISTRADOR"));
            Usuario recepcion = ensureUsuario("recepcion", "recepcion@clinicas.local", "Recep123!", "Recepcion", "Clinica");
            ensureUsuarioRol(recepcion, roles.get("RECEPCIONISTA"));
        }

        private Sistema ensureSistema(String nombre, String dominio) {
            return sistemaRepository.findByNombre(nombre).orElseGet(() -> {
                Sistema s = new Sistema(); s.setNombre(nombre); s.setDominio(dominio);
                s.setActivo(true);
                return sistemaRepository.save(s);
            });
        }

        private Rol ensureRol(String nombre, Sistema sistema) {
            return rolRepository.findByNombre(nombre).orElseGet(() -> {
                Rol r = new Rol(); r.setNombre(nombre); r.setDescripcion("Rol " + nombre);
                r.setSistema(sistema); r.setActivo(true);
                return rolRepository.save(r);
            });
        }

        private Permiso ensurePermiso(String nombre, String codigo) {
            return permisoRepository.findByCodigo(codigo).orElseGet(() -> {
                Permiso p = new Permiso(); p.setNombre(nombre); p.setCodigo(codigo);
                p.setDescripcion("Permiso: " + nombre); p.setActivo(true);
                return permisoRepository.save(p);
            });
        }

        private Modulo ensureModulo(Sistema sistema, String nombre, String ruta, String icono, int orden, Modulo moduloPadre) {
            return moduloRepository.findBySistemaId(sistema.getId()).stream()
                    .filter(m -> m.getNombre().equals(nombre)).findFirst()
                    .orElseGet(() -> {
                        Modulo m = new Modulo(); m.setSistema(sistema); m.setNombre(nombre);
                        m.setRuta(ruta); m.setIcono(icono); m.setOrden(orden);
                        m.setModuloPadre(moduloPadre);
                        m.setActivo(true);
                        return moduloRepository.save(m);
                    });
        }

        private RolModulo ensureRolModulo(Rol rol, Modulo modulo) {
            RolModulo rm = rolModuloRepository.findByRolIdRolAndModuloId(rol.getIdRol(), modulo.getId())
                    .orElseGet(() -> { RolModulo n = new RolModulo(); n.setRol(rol); n.setModulo(modulo); return n; });
            rm.setActivo(true);
            return rolModuloRepository.save(rm);
        }

        private void ensureRolModuloPermiso(RolModulo rolModulo, Permiso permiso) {
            RolModuloPermiso rmp = rolModuloPermisoRepository
                    .findByRolModuloIdAndPermisoIdPermiso(rolModulo.getId(), permiso.getIdPermiso())
                    .orElseGet(() -> { RolModuloPermiso n = new RolModuloPermiso(); n.setRolModulo(rolModulo); n.setPermiso(permiso); return n; });
            rmp.setActivo(true);
            rolModuloPermisoRepository.save(rmp);
        }

        private void ensureUsuarioRol(Usuario usuario, Rol rol) {
            UsuarioRol ur = usuarioRolRepository.findByUsuarioIdUsuarioAndRolIdRol(usuario.getIdUsuario(), rol.getIdRol())
                    .orElseGet(() -> { UsuarioRol n = new UsuarioRol(); n.setUsuario(usuario); n.setRol(rol); return n; });
            ur.setActivo(true);
            usuarioRolRepository.save(ur);
        }

        private Usuario ensureUsuario(String username, String email, String password, String nombres, String apellidos) {
            return usuarioRepository.findByUsername(username).orElseGet(() -> {
                Usuario u = new Usuario(); u.setUsername(username); u.setEmail(email);
                u.setPassword(passwordEncoder.encode(password));
                u.setNombres(nombres); u.setApellidos(apellidos);
                u.setActivo(true);
                return usuarioRepository.save(u);
            });
        }
    }
}
