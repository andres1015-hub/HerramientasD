package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.DetallePackage;

import java.time.LocalDateTime;

public class DetallePackageResponse {

    private Long idDetalles;
    private String codigoQR;
    private int cantidad;
    private LocalDateTime fechaEmision;

    // Paquete
    private Long idPaquete;
    private String nPedido;
    private String estadoPaquete;

    // Producto
    private Long idProducto;
    private String nombreProducto;
    private String tipoProducto;
    private double precioUnitario;

    // Usuario
    private Long idUsuario;
    private String nombreUsuario;
    private String apellidoUsuario;

    public DetallePackageResponse() {}

    public static DetallePackageResponse from(DetallePackage d) {
        DetallePackageResponse dto = new DetallePackageResponse();

        dto.idDetalles = d.getIdDetalles();
        dto.codigoQR = d.getCodigoQR();
        dto.cantidad = d.getCantidad();
        dto.fechaEmision = d.getFechaEmision();

        dto.idPaquete = d.getPaquete().getIdPaquete();
        dto.nPedido = d.getPaquete().getnPedido();
        dto.estadoPaquete = d.getPaquete().getEstado().name();

        dto.idProducto = d.getProducto().getIdProducto();
        dto.nombreProducto = d.getNombreProducto();
        dto.tipoProducto = d.getTipoProducto();
        dto.precioUnitario = d.getPrecioUnitario();

        if (d.getPaquete().getUsuario() != null) {
            dto.idUsuario = d.getPaquete().getUsuario().getIdUsuarios();
            dto.nombreUsuario = d.getPaquete().getUsuario().getNombre();
            dto.apellidoUsuario = d.getPaquete().getUsuario().getApelllido();
        }

        return dto;
    }

    public Long getIdDetalles() { return idDetalles; }
    public void setIdDetalles(Long idDetalles) { this.idDetalles = idDetalles; }

    public String getCodigoQR() { return codigoQR; }
    public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public Long getIdPaquete() { return idPaquete; }
    public void setIdPaquete(Long idPaquete) { this.idPaquete = idPaquete; }

    public String getnPedido() { return nPedido; }
    public void setnPedido(String nPedido) { this.nPedido = nPedido; }

    public String getEstadoPaquete() { return estadoPaquete; }
    public void setEstadoPaquete(String estadoPaquete) { this.estadoPaquete = estadoPaquete; }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getApellidoUsuario() { return apellidoUsuario; }
    public void setApellidoUsuario(String apellidoUsuario) { this.apellidoUsuario = apellidoUsuario; }
}