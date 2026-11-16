package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.Producto;

public class ProductoResponse {

    private Long idProducto;
    private String nombre;
    private String tipo;
    private int stock;
    private double precio;

    public static ProductoResponse from(Producto p) {
        ProductoResponse dto = new ProductoResponse();
        dto.idProducto = p.getIdProducto();
        dto.nombre = p.getNombre();
        dto.tipo = p.getTipo();
        dto.stock = p.getStock();
        dto.precio = p.getPrecio();
        return dto;
    }

    public Long getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public int getStock() { return stock; }
    public double getPrecio() { return precio; }
}
