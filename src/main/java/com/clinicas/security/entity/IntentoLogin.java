package com.clinicas.security.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro de auditoría en Base de Datos de cada intento de inicio de sesión.
 * Permite verificar intentos fallidos, bloqueos temporales e inicios exitosos.
 */
@Entity
@Table(name = "intento_login")
public class IntentoLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String username;

    @Column(length = 60)
    private String ip;

    @Column(nullable = false)
    private Boolean exitoso;

    @Column(length = 120)
    private String resultado;

    @Column(name = "intentos_acumulados")
    private Integer intentosAcumulados = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public IntentoLogin() {
    }

    public IntentoLogin(String username, String ip, Boolean exitoso, String resultado,
                        Integer intentosAcumulados, LocalDateTime bloqueadoHasta) {
        this.username = username;
        this.ip = ip;
        this.exitoso = exitoso;
        this.resultado = resultado;
        this.intentosAcumulados = intentosAcumulados;
        this.bloqueadoHasta = bloqueadoHasta;
        this.fechaHora = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaHora == null) {
            this.fechaHora = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Boolean getExitoso() {
        return exitoso;
    }

    public void setExitoso(Boolean exitoso) {
        this.exitoso = exitoso;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public Integer getIntentosAcumulados() {
        return intentosAcumulados;
    }

    public void setIntentosAcumulados(Integer intentosAcumulados) {
        this.intentosAcumulados = intentosAcumulados;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}
