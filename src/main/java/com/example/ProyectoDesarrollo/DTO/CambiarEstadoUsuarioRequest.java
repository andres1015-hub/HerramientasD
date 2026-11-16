package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.Usuario;
import jakarta.validation.constraints.NotNull;

public class CambiarEstadoUsuarioRequest {

    @NotNull(message = "El estado es obligatorio (ACTIVO, INNACTIVO o ELIMINADO)")
    private Usuario.Estado estado;

    public CambiarEstadoUsuarioRequest() {}

    public Usuario.Estado getEstado() { return estado; }
    public void setEstado(Usuario.Estado estado) { this.estado = estado; }
}
