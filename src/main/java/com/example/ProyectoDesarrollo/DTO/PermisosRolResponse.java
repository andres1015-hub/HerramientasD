package com.example.ProyectoDesarrollo.DTO;

public class PermisosRolResponse {

    private final String rol;
    private final String descripcion;

    public PermisosRolResponse(String rol, String descripcion) {
        this.rol = rol;
        this.descripcion = descripcion;
    }

    public String getRol() { return rol; }
    public String getDescripcion() { return descripcion; }
}
