package com.clinicas.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinicas.security.dto.auth.LoginRequest;
import com.clinicas.security.dto.auth.LoginResponse;
import com.clinicas.security.entity.Rol;
import com.clinicas.security.entity.Usuario;
import com.clinicas.security.entity.UsuarioRol;
import com.clinicas.security.repository.RolRepository;
import com.clinicas.security.repository.UsuarioRepository;
import com.clinicas.security.repository.UsuarioRolRepository;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityServiceApplicationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired UsuarioRolRepository usuarioRolRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtDecoder jwtDecoder;
    @Autowired JwtEncoder jwtEncoder;

    @BeforeEach
    void ensureAdminActive() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        admin.setActivo(true);
        usuarioRepository.save(admin);
    }

    @Test
    void loginValidoDevuelveJwtConClaimsRequeridos() throws Exception {
        String token = login("admin", "Admin123!");
        Jwt jwt = jwtDecoder.decode(token);

        assertThat(jwt.getClaimAsString("iss")).isEqualTo("clinicas-security");
        assertThat(jwt.getAudience()).contains("clinicas-backend");
        assertThat(jwt.getSubject()).isEqualTo("admin");
        assertThat(jwt.getClaimAsStringList("authorities")).contains("ROLE_ADMINISTRADOR", "USUARIO_READ", "ROL_UPDATE");
        assertThat((Object) jwt.getClaim("userId")).isNotNull();
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isNotNull();
    }

    @Test
    void passwordIncorrectoYUsuarioInexistenteDevuelven401() throws Exception {
        loginExpect("admin", "incorrecto", 401);
        loginExpect("noexiste", "Admin123!", 401);
    }

    @Test
    void usuarioDesactivadoDevuelve401() throws Exception {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        admin.setActivo(false);
        usuarioRepository.save(admin);

        loginExpect("admin", "Admin123!", 401);
    }

    @Test
    void endpointProtegidoSinTokenDevuelve401YConTokenValidoPermiteAcceso() throws Exception {
        mockMvc.perform(get("/api/security/usuarios"))
                .andExpect(status().isUnauthorized());

        String token = login("admin", "Admin123!");
        mockMvc.perform(get("/api/security/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    void jwtModificadoYJwtExpiradoDevuelven401() throws Exception {
        String token = login("admin", "Admin123!");
        String modificado = token.substring(0, token.length() - 2) + "xx";
        mockMvc.perform(get("/api/security/usuarios").header("Authorization", "Bearer " + modificado))
                .andExpect(status().isUnauthorized());

        String expired = expiredToken();
        mockMvc.perform(get("/api/security/usuarios").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioSinPermisoSuficienteDevuelve403() throws Exception {
        Usuario u = usuarioRepository.findByUsername("enfermera").orElseGet(() -> {
            Usuario nuevo = new Usuario();
            nuevo.setUsername("enfermera");
            nuevo.setPassword(passwordEncoder.encode("Enfermera123!"));
            nuevo.setActivo(true);
            return usuarioRepository.save(nuevo);
        });
        Rol rol = rolRepository.findByNombre("ENFERMERIA").orElseThrow();
        usuarioRolRepository.findByUsuarioIdUsuarioAndRolIdRol(u.getIdUsuario(), rol.getIdRol()).orElseGet(() -> {
            UsuarioRol ur = new UsuarioRol();
            ur.setUsuario(u);
            ur.setRol(rol);
            return usuarioRolRepository.save(ur);
        });

        String token = login("enfermera", "Enfermera123!");
        mockMvc.perform(get("/api/security/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, LoginResponse.class).accessToken();
    }

    private void loginExpect(String username, String password, int status) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().is(status));
    }

    private String expiredToken() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("clinicas-security")
                .audience(List.of("clinicas-backend"))
                .subject("admin")
                .issuedAt(now.minusSeconds(7200))
                .expiresAt(now.minusSeconds(3600))
                .claim("userId", admin.getIdUsuario())
                .claim("authorities", List.of("ROLE_ADMINISTRADOR", "USUARIO_READ"))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
    }
}
