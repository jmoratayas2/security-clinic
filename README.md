# Security Service De Clínica (`security-clinic`)

Microservicio backend independiente para autenticación, autorización y gestión de accesos jerárquicos del sistema de clínica. Desarrollado con **Spring Boot 4.1**, **Java 21**, **Spring Security 6**, **OAuth2 Resource Server** y **JWT firmado con RSA-256**.

---

## 📋 Requisitos Previos

- JDK 21 o superior.
- PostgreSQL 14+ para ejecución local (puerto 5432).
- Maven Wrapper incluido: `mvnw.cmd` (Windows) o `./mvnw` (Linux/macOS).
- OpenSSL (opcional, para regeneración de par de claves RSA).

---

## 🗄️ Base de Datos PostgreSQL

```sql
CREATE DATABASE clinicas_security;

CREATE USER clinicas_auth WITH PASSWORD 'ClinicasAuth123!';
GRANT ALL PRIVILEGES ON DATABASE clinicas_security TO clinicas_auth;

-- En PostgreSQL 15+:
\c clinicas_security
GRANT USAGE, CREATE ON SCHEMA public TO clinicas_auth;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO clinicas_auth;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO clinicas_auth;
```

---

## ⚙️ Configuración (`application.properties`)

```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/clinicas_security
spring.datasource.username=clinicas_auth
spring.datasource.password=${DB_PASSWORD:ClinicasAuth123!}
spring.jpa.hibernate.ddl-auto=update

security.jwt.issuer=clinicas-security
security.jwt.audience=clinicas-backend
security.jwt.expiration-seconds=3600
security.jwt.private-key=classpath:keys/private.pem
security.jwt.public-key=classpath:keys/public.pem

security.bootstrap.enabled=true
```

---

## 🏛️ Modelo de Datos (Nuevo ER - 8 Entidades)

El sistema implementa el modelo de 8 entidades para control jerárquico de accesos:

1. **`Sistema` (`sistema`)**: Agrupa módulos y roles por aplicación (`CLINICA_SYSTEM`).
2. **`Modulo` (`modulo`)**: Secciones funcionales con autorreferencia padre-hijo (`id_modulo_padre`), ruta, icono Material y orden.
3. **`Rol` (`rol`)**: Perfiles de usuario asociados a un sistema (`id_sistema`).
4. **`RolModulo` (`rol_modulo`)**: Relación única de asignación de módulos a roles (`UNIQUE(id_rol, id_modulo)`).
5. **`RolModuloPermiso` (`rol_modulo_permiso`)**: Permisos contextuales concedidos a un rol dentro de un módulo específico.
6. **`Permiso` (`permiso`)**: Catálogo técnico de acciones (`codigo` único, ej: `PACIENTE_READ`).
7. **`Usuario` (`usuario`)**: Cuentas de usuario con `nombres`, `apellidos`, `nombre_usuario`, `email` y `password_hash`.
8. **`UsuarioRol` (`usuario_rol`)**: Asignación de roles a usuarios.

---

## 📡 Endpoints REST

### Autenticación
- `POST /api/auth/login` — Autenticación por credenciales, devuelve JWT con claims `authorities`, `userId`, `iss`, `aud`.

### Menú Dinámico y Accesos (Serie II)
- **`GET /api/security/usuarios/me/menu`** — Obtiene el árbol jerárquico de módulos autorizados para el usuario autenticado (con rutas, iconos y submódulos anidados).
- **`GET /api/security/modulos/menu`** — Alias del endpoint de menú de accesos.

### Módulos
- `GET /api/security/modulos` — Listar todos los módulos (`MODULO_READ`).
- `GET /api/security/modulos/{id}` — Obtener detalle de módulo (`MODULO_READ`).
- `POST /api/security/modulos` — Crear módulo con soporte padre-hijo (`MODULO_CREATE`).
- `PUT /api/security/modulos/{id}` — Actualizar módulo (`MODULO_UPDATE`).
- `PATCH /api/security/modulos/{id}/estado` — Activar/desactivar módulo (`MODULO_UPDATE`).

### Sistemas
- `GET /api/security/sistemas` — Listar sistemas (`SISTEMA_READ`).
- `POST /api/security/sistemas` — Crear sistema (`SISTEMA_CREATE`).
- `GET /api/security/sistemas/{id}/modulos` — Listar módulos de un sistema (`MODULO_READ`).

### Roles y Permisos por Módulo
- `GET /api/security/roles` — Listar roles (`ROL_READ`).
- `POST /api/security/roles` — Crear rol asignado a un sistema (`ROL_CREATE`).
- `GET /api/security/roles/{id}/modulos` — Listar módulos asignados al rol.
- `POST /api/security/roles/{id}/modulos` — Asignar módulo al rol (`ROL_UPDATE`).
- `DELETE /api/security/roles/{id}/modulos/{idModulo}` — Desasignar módulo de rol.
- `POST /api/security/rol-modulo/{idRolModulo}/permisos` — Otorgar permiso en contexto de módulo (`ROL_UPDATE`).
- `DELETE /api/security/rol-modulo/{idRolModulo}/permisos/{idPermiso}` — Revocar permiso de módulo.

### Usuarios
- `GET /api/security/usuarios` — Listar usuarios (`USUARIO_READ`).
- `POST /api/security/usuarios` — Crear usuario (`USUARIO_CREATE`).
- `PUT /api/security/usuarios/{id}` — Actualizar usuario (`USUARIO_UPDATE`).
- `PATCH /api/security/usuarios/{id}/estado` — Activar/desactivar usuario (`USUARIO_UPDATE`).
- `POST /api/security/usuarios/{id}/roles` — Asignar rol a usuario (`USUARIO_UPDATE`).
- `PATCH /api/security/usuarios/{id}/roles/{rolId}/revocar` — Revocar rol (`USUARIO_UPDATE`).

---

## 🧪 Pruebas Automatizadas

El proyecto incluye pruebas de integración en `SecurityServiceApplicationTests`:

```bash
./mvnw.cmd clean test
```

Valida:
- Login exitoso con emisión de JWT RSA y claims requeridos.
- Rechazo por password inválido, usuario inexistente o inactivo (401).
- Protección de endpoints sin token (401) y con token no autorizado (403).
- **Menú dinámico jerárquico**: Estructura de módulos devuelta para `admin` vs `recepcion`.
