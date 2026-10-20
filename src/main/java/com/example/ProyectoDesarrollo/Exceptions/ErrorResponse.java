package com.example.ProyectoDesarrollo.Exceptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuerpo JSON estándar que devuelven las respuestas de error de la API.
 * Lo construyen los manejadores globales de excepciones.
 */
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int estado;
    private String error;
    private String mensaje;
    private String ruta;
    private List<CampoError> erroresCampo = new ArrayList<>();

    public ErrorResponse() {
    }

    public ErrorResponse(int estado, String error, String mensaje, String ruta) {
        this.timestamp = LocalDateTime.now();
        this.estado = estado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getEstado() { return estado; }
    public void setEstado(int estado) { this.estado = estado; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public List<CampoError> getErroresCampo() { return erroresCampo; }
    public void setErroresCampo(List<CampoError> erroresCampo) { this.erroresCampo = erroresCampo; }

    /**
     * Detalle de un campo inválido encontrado durante la validación.
     */
    public static class CampoError {

        private String campo;
        private String mensaje;

        public CampoError() {
        }

        public CampoError(String campo, String mensaje) {
            this.campo = campo;
            this.mensaje = mensaje;
        }

        public String getCampo() { return campo; }
        public void setCampo(String campo) { this.campo = campo; }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}
