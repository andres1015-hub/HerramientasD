package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.CambiarActivoProductoRequest;
import com.example.ProyectoDesarrollo.DTO.MovimientoStockRequest;
import com.example.ProyectoDesarrollo.DTO.MovimientoStockResponse;
import com.example.ProyectoDesarrollo.DTO.PaginaResponse;
import com.example.ProyectoDesarrollo.DTO.ProductoActualizarRequest;
import com.example.ProyectoDesarrollo.DTO.ProductoRequest;
import com.example.ProyectoDesarrollo.DTO.ProductoResponse;
import com.example.ProyectoDesarrollo.DTO.StockResponse;
import com.example.ProyectoDesarrollo.Models.MovimientoStock;
import com.example.ProyectoDesarrollo.Services.InventarioService;
import com.example.ProyectoDesarrollo.Services.MovimientoStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Catálogo e inventario: consulta, alta de productos y movimientos de stock. */
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final InventarioService inventarioService;
    private final MovimientoStockService movimientoService;

    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(name = "nombre", required = false) String nombre,
                                         @RequestParam(name = "tipo", required = false) String tipo) {
        return inventarioService.buscar(nombre, tipo).stream().map(ProductoResponse::from).toList();
    }

    @GetMapping("/stock-bajo")
    public List<ProductoResponse> stockBajo(@RequestParam(name = "minimo", defaultValue = "5") int minimo) {
        return inventarioService.productosStockBajo(minimo).stream().map(ProductoResponse::from).toList();
    }

    /** Alertas según el umbral propio (stockMinimo) de cada producto. */
    @GetMapping("/alertas")
    public List<ProductoResponse> alertas() {
        return inventarioService.productosEnAlerta().stream().map(ProductoResponse::from).toList();
    }

    /** Tipos distintos de los productos activos. */
    @GetMapping("/tipos")
    public List<String> tipos() {
        return inventarioService.tipos();
    }

    /** Listado paginado. {@code activo} solo lo respeta un ADMINISTRADOR; el resto ve únicamente activos. */
    @GetMapping("/paginado")
    public PaginaResponse<ProductoResponse> paginado(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "activo", required = false) Boolean activo,
            @RequestParam(name = "stockBajo", defaultValue = "false") boolean stockBajo,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio) {
        return inventarioService.buscarPaginado(nombre, tipo, activo, stockBajo, pagina, tamanio);
    }

    /** Historial de movimientos de un producto (solo ADMINISTRADOR). */
    @GetMapping("/{id}/movimientos")
    public PaginaResponse<MovimientoStockResponse> movimientos(
            @PathVariable Long id,
            @RequestParam(name = "tipo", required = false) MovimientoStock.Tipo tipo,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "20") int tamanio) {
        inventarioService.obtenerPorId(id);
        return movimientoService.buscar(id, tipo, desde, hasta, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return ProductoResponse.from(inventarioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductoResponse.from(inventarioService.crear(request)));
    }

    /** Edita nombre, tipo, precio y stock mínimo (solo ADMINISTRADOR). */
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody ProductoActualizarRequest request) {
        return ProductoResponse.from(inventarioService.actualizar(id, request));
    }

    /** Baja o alta lógica del producto (solo ADMINISTRADOR). */
    @PatchMapping("/{id}/activo")
    public ProductoResponse cambiarActivo(@PathVariable Long id,
                                          @Valid @RequestBody CambiarActivoProductoRequest request) {
        return ProductoResponse.from(inventarioService.cambiarActivo(id, request.getActivo()));
    }

    @PostMapping("/{id}/entrada")
    public StockResponse entrada(@PathVariable Long id, @Valid @RequestBody MovimientoStockRequest request) {
        return new StockResponse(id, inventarioService.registrarEntrada(id, request.getCantidad()));
    }

    @PostMapping("/{id}/salida")
    public StockResponse salida(@PathVariable Long id, @Valid @RequestBody MovimientoStockRequest request) {
        return new StockResponse(id, inventarioService.registrarSalida(id, request.getCantidad()));
    }

    /** Fija el stock a un valor exacto (solo ADMINISTRADOR). */
    @PutMapping("/{id}/stock")
    public StockResponse ajustar(@PathVariable Long id, @Valid @RequestBody MovimientoStockRequest request) {
        return new StockResponse(id, inventarioService.ajustarStock(id, request.getCantidad()));
    }
}
