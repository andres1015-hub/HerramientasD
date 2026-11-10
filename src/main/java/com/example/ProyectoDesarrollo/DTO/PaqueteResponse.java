package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PaqueteResponse {

    private Long idPaquete;

    @JsonProperty("nPedido")
    private String nPedido;

    private String estado;

    private Long idUsuario;
    private String nombreUsuario;
    private String apellidoUsuario;

    private int totalUnidades;
    private double total;
    private List<DetallePackageResponse> detalles = new ArrayList<>();

    public PaqueteResponse() {
    }

    public static PaqueteResponse from(Paquete paquete) {
        PaqueteResponse dto = new PaqueteResponse();
        dto.idPaquete = paquete.getIdPaquete();
        dto.nPedido = paquete.getnPedido();
        dto.estado = paquete.getEstado() != null ? paquete.getEstado().name() : null;

        if (paquete.getUsuario() != null) {
            dto.idUsuario = paquete.getUsuario().getIdUsuarios();
            dto.nombreUsuario = paquete.getUsuario().getNombre();
            dto.apellidoUsuario = paquete.getUsuario().getApelllido();
        }

        if (paquete.getDetalles() != null) {
            for (DetallePackage detalle : paquete.getDetalles()) {
                dto.detalles.add(DetallePackageResponse.from(detalle));
                dto.totalUnidades += detalle.getCantidad();
                dto.total += detalle.getCantidad() * detalle.getPrecioUnitario();
            }
        }
        return dto;
    }

    public Long getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(Long idPaquete) {
        this.idPaquete = idPaquete;
    }

    public String getnPedido() {
        return nPedido;
    }

    public void setnPedido(String nPedido) {
        this.nPedido = nPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public void setTotalUnidades(int totalUnidades) {
        this.totalUnidades = totalUnidades;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetallePackageResponse> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePackageResponse> detalles) {
        this.detalles = detalles;
    }
}
