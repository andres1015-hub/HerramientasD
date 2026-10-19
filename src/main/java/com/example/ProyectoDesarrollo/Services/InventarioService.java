package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Repositories.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return inventarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public int obtenerStock(Long idProducto) {
        Producto producto = inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
        return producto.getStock();
    }

    @Transactional(readOnly = true)
    public List<Producto> productosStockBajo(int minimo) {
        return inventarioRepository.findByStockLessThanEqual(minimo);
    }

    @Transactional(readOnly = true)
    public List<Producto> productosPorTipo(String tipo) {
        return inventarioRepository.findByTipo(tipo);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return inventarioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional
    public int registrarEntrada(Long idProducto, int cantidad) {
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

    @Transactional
    public int registrarSalida(Long idProducto, int cantidad) {
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

    @Transactional
    public int ajustarStock(Long idProducto, int nuevoStock) {
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
