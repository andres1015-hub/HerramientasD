package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DetalleQrRequest {

    @NotBlank(message = "El codigo QR es obligatorio")
    @Size(max = 100, message = "El codigo QR no puede superar los 100 caracteres")
    private String codigoQR;

    public DetalleQrRequest() {}

    public String getCodigoQR() { return codigoQR; }
    public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }
}
