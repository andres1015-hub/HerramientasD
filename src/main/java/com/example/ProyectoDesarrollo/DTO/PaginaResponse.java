package com.example.ProyectoDesarrollo.DTO;

import org.springframework.data.domain.Page;

import java.util.List;

/** Respuesta paginada estándar. */
public class PaginaResponse<T> {

    private final List<T> contenido;
    private final int pagina;
    private final int tamanio;
    private final long totalElementos;
    private final int totalPaginas;
    private final boolean ultima;

    public PaginaResponse(List<T> contenido, int pagina, int tamanio, long totalElementos) {
        this.contenido = contenido;
        this.pagina = pagina;
        this.tamanio = tamanio;
        this.totalElementos = totalElementos;
        this.totalPaginas = tamanio <= 0 ? 0 : (int) Math.ceil((double) totalElementos / tamanio);
        this.ultima = pagina + 1 >= this.totalPaginas;
    }

    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    /** Pagina una lista ya filtrada en memoria. */
    public static <T> PaginaResponse<T> de(List<T> todos, int pagina, int tamanio) {
        long desde = (long) pagina * tamanio;
        List<T> contenido = desde >= todos.size()
                ? List.of()
                : todos.subList((int) desde, (int) Math.min(todos.size(), desde + tamanio));
        return new PaginaResponse<>(contenido, pagina, tamanio, todos.size());
    }

    public List<T> getContenido() { return contenido; }
    public int getPagina() { return pagina; }
    public int getTamanio() { return tamanio; }
    public long getTotalElementos() { return totalElementos; }
    public int getTotalPaginas() { return totalPaginas; }
    public boolean isUltima() { return ultima; }
}
