package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ResumenPaqueteRequest {

    @Size(max = 50, message = "Un paquete no puede contener más de 50 productos distintos")
    private List<@Valid CrearPaqueteRequest.ItemPaquete> items = new ArrayList<>();

    public ResumenPaqueteRequest() {}

    public List<CrearPaqueteRequest.ItemPaquete> getItems() { return items; }
    public void setItems(List<CrearPaqueteRequest.ItemPaquete> items) { this.items = items; }
}
