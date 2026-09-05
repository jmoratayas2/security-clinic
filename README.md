# Security Service De Clinica

Servicio backend independiente para autenticacion y autorizacion del sistema de clinica. No contiene entidades clinicas ni relaciones JPA hacia `Medico`, `Paciente`, `Clinica`, `Cita` u otros objetos del dominio clinico.

## Requisitos

- JDK 21.
- PostgreSQL 14 o superior para desarrollo/ejecucion local.
- Maven Wrapper incluido: `mvnw.cmd` en Windows, `./mvnw` en Linux/macOS.
- OpenSSL para generar llaves RSA.

## Base De Datos PostgreSQL

Ejemplo de creacion local:

```sql
CREATE DATABASE clinicas_security;

CREATE USER clinicas_auth
WITH PASSWORD 'ClinicasAuth123!';

GRANT ALL PRIVILEGES
ON DATABASE clinicas_security
TO clinicas_auth;
```

Si PostgreSQL requiere permisos sobre el schema `public`, ejecutar conectado a `clinicas_security`:

```sql
GRANT USAGE, CREATE ON SCHEMA public TO clinicas_auth;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO clinicas_auth;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO clinicas_auth;
```

## Configuracion

Propiedades principales:

```properties
server.port=8081
spring.datasource.url=jdbc:postgresql://localhost:5432/clinicas_security
spring.datasource.username=clinicas_auth
spring.datasource.password=${DB_PASSWORD:ClinicasAuth123!}
security.jwt.issuer=clinicas-security
security.jwt.audience=clinicas-backend
security.jwt.expiration-seconds=3600
security.jwt.private-key=classpath:keys/private.pem
security.jwt.public-key=classpath:keys/public.pem
```

En desarrollo se usa `spring.jpa.hibernate.ddl-auto=update`. En produccion se debe usar `validate` con migraciones versionadas, por ejemplo Flyway.

## Llaves RSA

Generar llaves de desarrollo:

```bash
openssl genpkey -algorithm RSA -out src/main/resources/keys/private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in src/main/resources/keys/private.pem -out src/main/resources/keys/public.pem
```

Ubicaciones:

- Llave privada: `src/main/resources/keys/private.pem`.
- Llave publica: `src/main/resources/keys/public.pem`.

La llave privada esta en `.gitignore` y no debe compartirse. En produccion debe venir desde Secret Manager, Vault, Kubernetes Secret o un mecanismo seguro equivalente. El backend clinico solo debera recibir `public.pem`.

## Ejecutar

En Windows, usando JDK 21:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd spring-boot:run
```

El perfil por defecto es `dev`.

## Probar

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Las pruebas usan H2 y llaves RSA temporales; no requieren PostgreSQL ni `private.pem`.

## Login

Usuario inicial de desarrollo:

- Username: `admin`
- Password: `Admin123!`
- Rol: `ADMINISTRADOR`

Ejemplo con curl:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}'
```

Respuesta:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## JWT

El token se firma con RSA/RS256 e incluye:

- `iss`: `clinicas-security`
- `aud`: `clinicas-backend`
- `sub`: username
- `iat`
- `exp`
- `userId`
- `authorities`
- `medicoId`, solo si el usuario lo tiene configurado

No se incluyen datos clinicos sensibles dentro del JWT.

## Endpoints

- `POST /api/auth/login`
- `GET /api/security/usuarios`
- `GET /api/security/usuarios/{id}`
- `POST /api/security/usuarios`
- `PUT /api/security/usuarios/{id}`
- `PATCH /api/security/usuarios/{id}/estado`
- `POST /api/security/usuarios/{id}/roles`
- `PATCH /api/security/usuarios/{id}/roles/{rolId}/revocar`
- `GET /api/security/roles`
- `GET /api/security/roles/{id}`
- `POST /api/security/roles`
- `PUT /api/security/roles/{id}`
- `PATCH /api/security/roles/{id}/estado`
- `POST /api/security/roles/{id}/permisos`
- `PATCH /api/security/roles/{id}/permisos/{permisoId}/revocar`
- `GET /api/security/permisos`
- `GET /api/security/permisos/{id}`
- `POST /api/security/permisos`
- `PUT /api/security/permisos/{id}`
- `PATCH /api/security/permisos/{id}/estado`

Los endpoints administrativos usan `@PreAuthorize` con permisos como `USUARIO_READ`, `ROL_UPDATE` o `PERMISO_CREATE`.

## Modelo De Base De Datos

- `usuario`: identidad de login, password BCrypt, email, estado, `medico_id` como referencia logica opcional.
- `rol`: roles del sistema.
- `permiso`: acciones concretas sobre modulos.
- `usuario_rol`: asignacion explicita usuario-rol, con estado y fechas.
- `rol_permiso`: asignacion explicita rol-permiso, con estado.

No hay `@ManyToMany` directo. No hay relaciones JPA hacia entidades clinicas.

## Roles Iniciales

- `ADMINISTRADOR`
- `RECEPCIONISTA`
- `MEDICO`
- `ENFERMERIA`
- `LABORATORIO`
- `AUDITOR`

`ADMINISTRADOR` recibe todos los permisos. `AUDITOR` solo permisos de lectura. `RECEPCIONISTA`, `MEDICO`, `ENFERMERIA` y `LABORATORIO` reciben la matriz definida para el flujo clinico.

## Permisos Iniciales

Incluye permisos de lectura, creacion y actualizacion para clinicas, medicos, especialidades, pacientes, citas, consultas, diagnosticos, tratamientos, recetas, medicamentos, examenes, resultados y administracion de seguridad.

Ejemplos:

- `PACIENTE_READ`
- `CITA_CREATE`
- `CONSULTA_CREATE`
- `RECETA_ANULAR`
- `RESULTADO_EXAMEN_CREATE`
- `USUARIO_UPDATE`
- `ROL_UPDATE`
- `PERMISO_UPDATE`

## Integracion Futura Con ClinicaBK

El backend clinico debera copiar solamente:

```text
src/main/resources/keys/public.pem
```

Nunca debe copiar `private.pem`.

Luego debera validar:

- Firma RSA.
- `exp`.
- `iss`.
- `aud`.
- Claim `authorities` como authorities de Spring Security.

Esto permitira usar reglas como:

```java
@PreAuthorize("hasAuthority('CONSULTA_CREATE')")
@PreAuthorize("hasRole('MEDICO')")
```
