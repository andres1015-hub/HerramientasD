package com.example.ProyectoDesarrollo.Util;

import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Validación común de los parámetros de paginación. */
public final class Paginacion {

    public static final int TAMANIO_MAXIMO = 100;

    private Paginacion() {
    }

    public static Pageable de(int pagina, int tamanio, Sort orden) {
        validar(pagina, tamanio);
        return PageRequest.of(pagina, tamanio, orden);
    }

    public static void validar(int pagina, int tamanio) {
        if (pagina < 0) {
            throw new DatosInvalidosException("La página no puede ser negativa");
        }
        if (tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new DatosInvalidosException("El tamaño de página debe estar entre 1 y " + TAMANIO_MAXIMO);
        }
    }
}
