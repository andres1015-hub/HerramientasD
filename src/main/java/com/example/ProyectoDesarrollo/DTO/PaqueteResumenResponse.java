package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class PaqueteResumenResponse {
    private Long idPaquete;

    @JsonProperty("nPedido")
    private String nPedido;

    private String estado;
    private String nomberUsuario;
    private String apellidoUsuario;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCambioEstado;
    private String motivoCancelacion;
    private int productos;
    private int totalUnidades;
    private double total;

    public static PaqueteResumenResponse from(Paquete p){
        PaqueteResumenResponse dto=new PaqueteResumenResponse();
        dto.idPaquete=p.getIdPaquete();
        dto.nPedido=p.getnPedido();
        dto.estado=p.getEstado() !=null ? p.getEstado().name(): null;
        if(p.getUsuario()!=null){
            dto.nomberUsuario=p.getUsuario().getNombre();
            dto.apellidoUsuario=p.getUsuario().getApelllido();
        }
        dto.fechaCreacion=p.getFechaCreacion();
        dto.fechaCambioEstado=p.getFechaCambioEstado();
        dto.motivoCancelacion=p.getMotivoCancelacion();
        if(p.getDetalles() != null){
            dto.productos=p.getDetalles().size();
            double suma=0;
            for(DetallePackage d:p.getDetalles()){
                dto.totalUnidades+=d.getCantidad();
                suma+=d.getCantidad()*d.getPrecioUnitario();
            }
            dto.total=Math.round(suma*100.0)/100.0;
        }
        return dto;
    }

    public Long getIdPaquete() {
        return idPaquete;
    }

    public String getnPedido() {
        return nPedido;
    }

    public String getEstado() {
        return estado;
    }

    public String getNomberUsuario() {
        return nomberUsuario;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCambioEstado() {
        return fechaCambioEstado;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public int getProductos() {
        return productos;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public double getTotal() {
        return total;
    }
}
