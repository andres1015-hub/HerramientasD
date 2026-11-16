package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MovimientoStockRequest {

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Max(value = 1000000, message = "La cantidad máxima es 1000000")
    private Integer cantidad;

    public MovimientoStockRequest() {}

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
