package com.example.ProyectoDesarrollo.Exceptions;

/**
 * Excepción de negocio: la operación solicitada viola una regla del sistema
 * (por ejemplo: stock insuficiente, estado inválido para la transición, etc.).
 * Los manejadores globales la traducen automáticamente a un error HTTP 409.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
