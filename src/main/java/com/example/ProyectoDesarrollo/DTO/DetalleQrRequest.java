package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.constraints.NotBlank;

public class DetalleQrRequest {
    @NotBlank(message="El codigo QR es obligatorio")
    private String codigoQR;
    public DetalleQrRequest() {}
    public String getCodigoQR() {return codigoQR;}
    public void setCodigoQR(String codigoQR) {this.codigoQR = codigoQR;}
}
