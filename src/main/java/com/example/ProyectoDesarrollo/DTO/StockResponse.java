package com.example.ProyectoDesarrollo.DTO;

public class StockResponse {

    private final Long idProducto;
    private final int stock;

    public StockResponse(Long idProducto, int stock) {
        this.idProducto = idProducto;
        this.stock = stock;
    }

    public Long getIdProducto() { return idProducto; }
    public int getStock() { return stock; }
}
