package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.MovimientoStockRequest;
import com.example.ProyectoDesarrollo.DTO.ProductoRequest;
import com.example.ProyectoDesarrollo.DTO.ProductoResponse;
import com.example.ProyectoDesarrollo.DTO.StockResponse;
import com.example.ProyectoDesarrollo.Services.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final InventarioService inventarioService;

    @GetMapping
    public List<ProductoResponse> listar(@RequestParam(name = "nombre", required = false) String nombre,
                                         @RequestParam(name = "tipo", required = false) String tipo) {
        return inventarioService.buscar(nombre, tipo).stream().map(ProductoResponse::from).toList();
    }

    @GetMapping("/stock-bajo")
    public List<ProductoResponse> stockBajo(@RequestParam(name = "minimo", defaultValue = "5") int minimo) {
        return inventarioService.productosStockBajo(minimo).stream().map(ProductoResponse::from).toList();
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

    @PostMapping("/{id}/entrada")
    public StockResponse entrada(@PathVariable Long id, @Valid @RequestBody MovimientoStockRequest request) {
        return new StockResponse(id, inventarioService.registrarEntrada(id, request.getCantidad()));
    }

    @PostMapping("/{id}/salida")
    public StockResponse salida(@PathVariable Long id, @Valid @RequestBody MovimientoStockRequest request) {
        return new StockResponse(id, inventarioService.registrarSalida(id, request.getCantidad()));
    }

    @PutMapping("/{id}/stock")
    public StockResponse ajustar(@PathVariable Long id, @Valid @RequestBody MovimientoStockRequest request) {
        return new StockResponse(id, inventarioService.ajustarStock(id, request.getCantidad()));
    }
}
