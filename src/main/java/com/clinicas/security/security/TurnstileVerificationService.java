package com.clinicas.security.security;

import com.clinicas.security.exception.TurnstileValidationException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Servicio para verificar el token de Cloudflare Turnstile directamente contra los servidores de Cloudflare.
 * Endpoint oficial: https://challenges.cloudflare.com/turnstile/v0/siteverify
 */
@Service
public class TurnstileVerificationService {

    private static final Logger log = LoggerFactory.getLogger(TurnstileVerificationService.class);
    private static final String TURNSTILE_VERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    private final String secretKey;
    private final boolean enabled;
    private final RestClient restClient;

    public TurnstileVerificationService(
            @Value("${security.turnstile.secret-key:1x0000000000000000000000000000000AA}") String secretKey,
            @Value("${security.turnstile.enabled:true}") boolean enabled) {
        this.secretKey = secretKey;
        this.enabled = enabled;
        this.restClient = RestClient.builder().build();
    }

    public record TurnstileResponse(
            boolean success,
            @JsonProperty("challenge_ts") String challengeTs,
            String hostname,
            @JsonProperty("error-codes") List<String> errorCodes
    ) {}

    /**
     * Valida el token de Cloudflare Turnstile.
     * @param token El token recibido desde el frontend
     * @param clientIp La IP del cliente solicitante (opcional)
     * @throws TurnstileValidationException si el token es nulo, vacío o rechazado por Cloudflare
     */
    public void verify(String token, String clientIp) {
        if (!enabled) {
            log.info("Turnstile verification disabled by configuration.");
            return;
        }

        if (token == null || token.isBlank()) {
            throw new TurnstileValidationException("El token de verificacion de CAPTCHA / Turnstile es requerido");
        }

        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("secret", secretKey);
            body.add("response", token.trim());
            if (clientIp != null && !clientIp.isBlank()) {
                body.add("remoteip", clientIp);
            }

            TurnstileResponse response = restClient.post()
                    .uri(TURNSTILE_VERIFY_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(TurnstileResponse.class);

            if (response == null || !response.success()) {
                log.warn("Cloudflare Turnstile verification failed. Error codes: {}",
                        response != null ? response.errorCodes() : "null response");
                throw new TurnstileValidationException("Validacion de CAPTCHA / Cloudflare Turnstile fallida");
            }

            log.info("Cloudflare Turnstile token validado exitosamente para hostname: {}", response.hostname());
        } catch (TurnstileValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error al comunicarse con Cloudflare Turnstile API: {}", ex.getMessage());
            // Si el token es de testing o falló la conexión externa, propagar error claro
            throw new TurnstileValidationException("Error al validar el CAPTCHA con Cloudflare: " + ex.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
