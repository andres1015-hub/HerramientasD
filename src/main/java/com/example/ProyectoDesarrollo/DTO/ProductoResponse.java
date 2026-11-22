package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.Producto;

public class ProductoResponse {

    private Long idProducto;
    private String nombre;
    private String tipo;
    private int stock;
    private double precio;
    private boolean activo;
    private int stockMinimo;

    public static ProductoResponse from(Producto p) {
        ProductoResponse dto = new ProductoResponse();
        dto.idProducto = p.getIdProducto();
        dto.nombre = p.getNombre();
        dto.tipo = p.getTipo();
        dto.stock = p.getStock();
        dto.precio = p.getPrecio();
        dto.activo = p.isActivo();
        dto.stockMinimo = p.getStockMinimo();
        return dto;
    }

    public Long getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public int getStock() { return stock; }
    public double getPrecio() { return precio; }
    public boolean isActivo() { return activo; }
    public int getStockMinimo() { return stockMinimo; }
}
