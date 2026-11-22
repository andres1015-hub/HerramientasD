package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PerfilActualizarRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre no puede superar los 30 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligaio")
    @Size(max = 30, message = "El apellido no puede superar los 30 caracteres")
    private String apellido;

    public PerfilActualizarRequest(){}

    public String getNombre(){return nombre;}
    public void setNombre(String nombre){this.nombre=nombre;}

    public String getApellido(){return apellido;}
    public void setApellido(String apellido){this.apellido=apellido;}
}
