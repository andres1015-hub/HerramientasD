package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.Paquete;
import jakarta.validation.constraints.NotNull;

public class CambiarEstadoPaqueteRequest {

    @NotNull(message = "El estado es obligatorio (Enviado, Recibido o Cancelado)")
    private Paquete.Estado estado;

    public CambiarEstadoPaqueteRequest() {}

    public Paquete.Estado getEstado() { return estado; }
    public void setEstado(Paquete.Estado estado) { this.estado = estado; }
}
