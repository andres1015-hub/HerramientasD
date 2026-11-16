package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.DTO.CrearPaqueteRequest.ItemPaquete;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/** Productos seleccionados en el empaquetador, para calcular el resumen antes de crear el paquete. */
public class ResumenPaqueteRequest {

    @Size(max = 50, message = "Un paquete no puede contener más de 50 productos distintos")
    private List<@Valid ItemPaquete> items = new ArrayList<>();

    public ResumenPaqueteRequest() {}

    public List<ItemPaquete> getItems() { return items; }
    public void setItems(List<ItemPaquete> items) { this.items = items; }
}
