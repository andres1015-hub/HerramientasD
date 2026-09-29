package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.DetallePackage;

public class DetallePackageResponse {

    private Long idDetalles;
    private String codigoQR;
    private int cantidad;

    // Nota Andrés:
    // estes es el bloque de paquete
    private Long idPaquete;
    private String nPedido;
    private String estadoPaquete;

    // Nota Andrés:
    // este es el bloque del producto
    private Long idProducto;
    private String nombreProducto;
    private String tipoProducto;
    private int stockProducto;
    private double precioProducto;

    // Nota Andrés:
    //relación de usuario
    private Long idUsuario;
    private String nombreUsuario;
    private String apellidoUsuario;

    public DetallePackageResponse() {}

    public static DetallePackageResponse from(DetallePackage d) {
        DetallePackageResponse dto = new DetallePackageResponse();

        dto.idDetalles = d.getIdDetalles();
        dto.codigoQR = d.getCodigoQR();
        dto.cantidad = d.getCantidad();

        dto.idPaquete = d.getPaquete().getIdPaquete();
        dto.nPedido = d.getPaquete().getnPedido();
        dto.estadoPaquete = d.getPaquete().getEstado().name();

        dto.idProducto = d.getProducto().getIdProducto();
        dto.nombreProducto = d.getProducto().getNombre();
        dto.tipoProducto = d.getProducto().getTipo();
        dto.stockProducto = d.getProducto().getStock();
        dto.precioProducto = d.getProducto().getPrecio();

        if (d.getPaquete().getUsuario() != null) {
            dto.idUsuario = d.getPaquete().getUsuario().getIdUsuarios();
            dto.nombreUsuario = d.getPaquete().getUsuario().getNombre();
            dto.apellidoUsuario = d.getPaquete().getUsuario().getApelllido();
        }

        return dto;
    }

    public Long getIdDetalles() {
        return idDetalles;
    }

    public void setIdDetalles(Long idDetalles) {
        this.idDetalles = idDetalles;
    }

    public String getCodigoQR() {
        return codigoQR;
    }

    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }

    public Long getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(Long idPaquete) {
        this.idPaquete = idPaquete;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getnPedido() {
        return nPedido;
    }

    public void setnPedido(String nPedido) {
        this.nPedido = nPedido;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public double getPrecioProducto() {
        return precioProducto;
    }

    public void setPrecioProducto(double precioProducto) {
        this.precioProducto = precioProducto;
    }

    public int getStockProducto() {
        return stockProducto;
    }

    public void setStockProducto(int stockProducto) {
        this.stockProducto = stockProducto;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto = idProducto;
    }

    public String getEstadoPaquete() {
        return estadoPaquete;
    }

    public void setEstadoPaquete(String estadoPaquete) {
        this.estadoPaquete = estadoPaquete;
    }
}