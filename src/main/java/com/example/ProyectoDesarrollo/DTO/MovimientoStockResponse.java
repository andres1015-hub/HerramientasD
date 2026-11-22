package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.MovimientoStock;

import java.time.LocalDateTime;

public class MovimientoStockResponse {
    private Long idMovimiento;
    private Long idProducto;
    private String nombreProducto;
    private String tipo;
    private int cantidad;
    private int stockAnterior;
    private int stockNuevo;
    private String referencia;
    private String usuarioDni;
    private LocalDateTime fecha;

    public static MovimientoStockResponse from(MovimientoStock m){
        MovimientoStockResponse dto = new MovimientoStockResponse();
        dto.idMovimiento=m.getIdMovimiento();
        dto.idProducto=m.getProducto().getIdProducto();
        dto.nombreProducto=m.getProducto().getNombre();
        dto.tipo=m.getTipo().name();
        dto.cantidad=m.getCantidad();
        dto.stockAnterior=m.getStockAnterior();
        dto.stockNuevo=m.getStockNuevo();
        dto.referencia=m.getReferencia();
        dto.usuarioDni=m.getUsuarioDni();
        dto.fecha=m.getFecha();
        return dto;
    }

    public Long getIdMovimiento() {
        return idMovimiento;
    }

    public Long getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getTipo() {
        return tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public int getStockAnterior() {
        return stockAnterior;
    }

    public int getStockNuevo() {
        return stockNuevo;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getUsuarioDni() {
        return usuarioDni;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
