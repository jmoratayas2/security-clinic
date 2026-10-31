package com.clinicas.security;

import com.clinicas.security.dto.auth.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityControlsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Control 1: Rate Limiting rechaza solicitudes tras 10 peticiones por minuto con HTTP 429")
    void testRateLimiting10PorMinuto() throws Exception {
        String testIp = "192.168.200.77";

        // Realizar 10 solicitudes con usuarios distintos para aislar el Rate Limiting por IP
        for (int i = 1; i <= 10; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", testIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest("user_rate_" + i, "wrong_pass", null))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().exists("X-RateLimit-Limit"))
                    .andExpect(header().string("X-RateLimit-Remaining", String.valueOf(10 - i)));
        }

        // La 11ª solicitud DEBE ser rechazada inmediatamente con HTTP 429 Too Many Requests
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", testIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user_rate_11", "wrong_pass", null))))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"))
                .andExpect(jsonPath("$.message").value("Demasiadas solicitudes. Limite excedido: 10 peticiones por minuto."));
    }

    @Test
    @DisplayName("Control 2 y 3: Identifica fallos consecutivos y bloquea la cuenta 15 minutos al 5º intento fallido")
    void testIntentosFallidosYBloqueo15Min() throws Exception {
        String targetUser = "bloqueo_user_" + System.currentTimeMillis();

        // 4 intentos fallidos desde IPs distintas para no activar Rate Limiting por IP
        for (int i = 1; i <= 4; i++) {
            int remaining = 5 - i;
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", "172.16.0." + i)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new LoginRequest(targetUser, "wrong_password", null))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(
                            "Credenciales invalidas. Intento fallido " + i + " de 5 (quedan " + remaining + " intentos antes del bloqueo temporal)."));
        }

        // 5º intento fallido: Bloquea inmediatamente la cuenta por 15 minutos (HTTP 423 Locked)
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", "172.16.0.5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(targetUser, "wrong_password", null))))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("ACCOUNT_LOCKED"))
                .andExpect(jsonPath("$.message").value(
                        "Ha alcanzado el límite de 5 intentos fallidos consecutivos. La cuenta ha sido bloqueada temporalmente durante 15 minutos."));

        // 6º intento: La cuenta permanece bloqueada impidiendo cualquier nuevo intento durante 15 minutos
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", "172.16.0.6")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(targetUser, "cualquier_password", null))))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("ACCOUNT_LOCKED"));
    }
}
