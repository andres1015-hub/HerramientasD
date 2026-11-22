package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.ResumenGeneralResponse;
import com.example.ProyectoDesarrollo.Models.MovimientoStock;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.example.ProyectoDesarrollo.Services.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Indicadores y exportaciones CSV (solo ADMINISTRADOR, validado en el servicio). */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private static final MediaType CSV = MediaType.parseMediaType("text/csv; charset=UTF-8");

    private final ReporteService reporteService;

    @GetMapping("/resumen")
    public ResumenGeneralResponse resumen() {
        return reporteService.resumen();
    }

    @GetMapping("/productos.csv")
    public ResponseEntity<byte[]> productos() {
        return csv("productos.csv", reporteService.productosCsv());
    }

    @GetMapping("/paquetes.csv")
    public ResponseEntity<byte[]> paquetes(
            @RequestParam(name = "estado", required = false) Paquete.Estado estado,
            @RequestParam(name = "pedido", required = false) String pedido,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return csv("paquetes.csv", reporteService.paquetesCsv(estado, pedido, desde, hasta));
    }

    @GetMapping("/movimientos.csv")
    public ResponseEntity<byte[]> movimientos(
            @RequestParam(name = "idProducto", required = false) Long idProducto,
            @RequestParam(name = "tipo", required = false) MovimientoStock.Tipo tipo,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return csv("movimientos.csv", reporteService.movimientosCsv(idProducto, tipo, desde, hasta));
    }

    private static ResponseEntity<byte[]> csv(String archivo, byte[] contenido) {
        return ResponseEntity.ok()
                .contentType(CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo + "\"")
                .body(contenido);
    }
}
