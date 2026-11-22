package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.AuditoriaEvento;

import java.time.LocalDateTime;
public class AuditoriaResponse {

    private Long idEvento;
    private LocalDateTime fecha;
    private String usuarioDni;
    private String accion;
    private String entidad;
    private Long entidadId;
    private String detalle;

    public static AuditoriaResponse from(AuditoriaEvento e){
        AuditoriaResponse dto=new AuditoriaResponse();
        dto.idEvento=e.getIdEvento();
        dto.fecha=e.getFecha();
        dto.usuarioDni=e.getUsuarioDni();
        dto.accion=e.getAccion();
        dto.entidad=e.getEntidad();
        dto.entidadId=e.getEntidadId();
        dto.detalle=e.getDetalle();
        return dto;
    }

    public Long getIdEvento() {
        return idEvento;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getUsuarioDni() {
        return usuarioDni;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getDetalle() {
        return detalle;
    }
}
