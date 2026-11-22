package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.MovimientoStockResponse;
import com.example.ProyectoDesarrollo.DTO.PaginaResponse;
import com.example.ProyectoDesarrollo.Models.MovimientoStock;
import com.example.ProyectoDesarrollo.Services.MovimientoStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Historial general de movimientos de stock (solo ADMINISTRADOR, validado en el servicio). */
@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoStockService movimientoService;

    @GetMapping
    public PaginaResponse<MovimientoStockResponse> listar(
            @RequestParam(name = "idProducto", required = false) Long idProducto,
            @RequestParam(name = "tipo", required = false) MovimientoStock.Tipo tipo,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "20") int tamanio) {
        return movimientoService.buscar(idProducto, tipo, desde, hasta, pagina, tamanio);
    }
}
