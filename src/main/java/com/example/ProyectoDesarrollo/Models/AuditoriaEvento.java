package com.example.ProyectoDesarrollo.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Bitácora de operaciones de negocio: quién hizo qué y cuándo. */
@Entity
@Table(name = "auditoria_evento")
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento", nullable = false)
    private Long idEvento;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "usuario_dni", length = 20)
    private String usuarioDni;

    /** Ej.: PRODUCTO_CREADO, USUARIO_ESTADO, PAQUETE_ESTADO. */
    @Column(name = "accion", nullable = false, length = 40)
    private String accion;

    /** Ej.: PRODUCTO, USUARIO, PAQUETE. */
    @Column(name = "entidad", nullable = false, length = 30)
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(name = "detalle", length = 500)
    private String detalle;

    @PrePersist
    void antesDeGuardar() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public AuditoriaEvento() {
    }

    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getUsuarioDni() { return usuarioDni; }
    public void setUsuarioDni(String usuarioDni) { this.usuarioDni = usuarioDni; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public Long getEntidadId() { return entidadId; }
    public void setEntidadId(Long entidadId) { this.entidadId = entidadId; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
}
