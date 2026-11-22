package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.constraints.*;

public class ProductoActualizarRequest {
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    private String nombre;

    @NotBlank(message = "El tipo de producto es obligatorio")
    @Size(max = 30, message = "El tipo no puede superar los 30 caracteres")
    private String tipo;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0",inclusive = false, message = "El precio debe de ser mayor a cero")
    private Double precio;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value=0, message = "El stock minimo no puede ser negativo")
    @Max(value = 1000000, message = "El stock minimo maximo es 1000000")
    private Integer stockMinimo;

    public ProductoActualizarRequest(){}

    public String getNombre(){return nombre;}
    public void setNombre(String nombre){this.nombre=nombre;}
    public String getTipo(){return tipo;}
    public void setTipo(String tipo){this.tipo=tipo;}
    public Double getPrecio(){return precio;}
    public void setPrecio(Double precio){this.precio=precio;}
    public Integer getStockMinimo(){return stockMinimo;}
    public void setStockMinimo(Integer stockMinimo){this.stockMinimo=stockMinimo;}
}
