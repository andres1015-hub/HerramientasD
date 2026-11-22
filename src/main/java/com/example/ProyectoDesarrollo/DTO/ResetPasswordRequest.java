package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 8, max = 100, message = "La contrasena debe de tener entre 8 y 100 caracteres")
    private String passwordNueva;

    public ResetPasswordRequest(){}

    public String getPasswordNueva(){return passwordNueva;}

    public void setPasswordNueva(String passwordNueva) {this.passwordNueva = passwordNueva;}
}
