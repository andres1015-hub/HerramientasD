package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.Paquete;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CambiarEstadoPaqueteRequest {

    @NotNull(message = "El estado es obligatorio (Enviado, Recibido o Cancelado)")
    private Paquete.Estado estado;

    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;

    public CambiarEstadoPaqueteRequest() {}

    public Paquete.Estado getEstado() { return estado; }
    public void setEstado(Paquete.Estado estado) { this.estado = estado; }

    public String getMotivo(){return motivo;}
    public void setMotivo(String motivo){this.motivo=motivo;}
}
