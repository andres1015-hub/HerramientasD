package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Repositories.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private static final int MAX_NOMBRE = 100;
    private static final int MAX_TIPO = 50;

    private final InventarioRepository inventarioRepository;

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return inventarioRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public int obtenerStock(Long idProducto) {
        if (idProducto == null || idProducto <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador del producto no es valido");
        }
        Producto producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
        return producto.getStock();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> productosStockBajo(int minimo) {
        if (minimo < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El minimo no puede ser negativo");
        }
        return inventarioRepository.findByStockLessThanEqual(minimo);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> productosPorTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo es obligatorio");
        }
        String valor = tipo.trim();
        if (valor.length() > MAX_TIPO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo es demasiado largo");
        }
        return inventarioRepository.findByTipo(valor);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return inventarioRepository.findAll();
        }
        String valor = nombre.trim();
        if (valor.length() > MAX_NOMBRE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es demasiado largo");
        }
        return inventarioRepository.findByNombreContainingIgnoreCase(valor);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public int registrarEntrada(Long idProducto, int cantidad) {
        if (idProducto == null || idProducto <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador del producto no es valido");
        }
        if (cantidad <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a cero");
        }
        Producto producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
        producto.setStock(producto.getStock() + cantidad);
        inventarioRepository.save(producto);
        return producto.getStock();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public int registrarSalida(Long idProducto, int cantidad) {
        if (idProducto == null || idProducto <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador del producto no es valido");
        }
        if (cantidad <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor a cero");
        }
        Producto producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
        if (producto.getStock() < cantidad) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Stock insuficiente");
        }
        producto.setStock(producto.getStock() - cantidad);
        inventarioRepository.save(producto);
        return producto.getStock();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public int ajustarStock(Long idProducto, int nuevoStock) {
        if (idProducto == null || idProducto <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador del producto no es valido");
        }
        if (nuevoStock < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El stock no puede ser negativo");
        }
        Producto producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
        producto.setStock(nuevoStock);
        inventarioRepository.save(producto);
        return producto.getStock();
    }
}
