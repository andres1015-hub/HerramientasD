package com.example.ProyectoDesarrollo.Exceptions;

/**
 * Excepción de negocio: el recurso solicitado (producto, usuario, detalle, etc.)
 * no existe o ya no está disponible.
 * Los manejadores globales la traducen automáticamente a un error HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
