package com.example.ProyectoDesarrollo.Exceptions;

/**
 * Excepción de validación: los datos enviados por el cliente no son válidos
 * (nulos, vacíos, fuera de rango, formato incorrecto, etc.).
 * Los manejadores globales la traducen automáticamente a un error HTTP 400.
 */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
