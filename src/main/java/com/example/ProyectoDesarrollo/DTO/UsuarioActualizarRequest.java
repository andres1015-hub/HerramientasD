package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Edición de nombre, apellido y rol de un usuario por un administrador. */
public class UsuarioActualizarRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre no puede superar los 30 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 30, message = "El apellido no puede superar los 30 caracteres")
    private String apellido;

    @NotNull(message = "El rol es obligatorio")
    private Usuario.Rol rol;

    public UsuarioActualizarRequest() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Usuario.Rol getRol() { return rol; }
    public void setRol(Usuario.Rol rol) { this.rol = rol; }
}
