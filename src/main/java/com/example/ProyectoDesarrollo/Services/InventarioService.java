package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.ProductoRequest;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Repositories.InventarioRepository;
import com.example.ProyectoDesarrollo.Repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private static final int MAX_STOCK = 1_000_000;

    private final InventarioRepository inventarioRepository;
    private final ProductRepository productoRepository;

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return inventarioRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long idProducto) {
        return inventarioRepository.findById(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public int obtenerStock(Long idProducto) {
        return obtenerPorId(idProducto).getStock();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> productosStockBajo(int minimo) {
        if (minimo < 0) {
            throw new DatosInvalidosException("El mínimo no puede ser negativo");
        }
        return inventarioRepository.findByStockLessThanEqual(minimo);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> productosPorTipo(String tipo) {
        return inventarioRepository.findByTipo(tipo);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return inventarioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /** Búsqueda combinada del catálogo: nombre (parcial) y tipo (exacto, sin distinguir mayúsculas). */
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<Producto> buscar(String nombre, String tipo) {
        String n = nombre == null ? "" : nombre.trim();
        String t = tipo == null ? "" : tipo.trim();
        return inventarioRepository.findAll().stream()
                .filter(p -> n.isEmpty() || p.getNombre().toLowerCase().contains(n.toLowerCase()))
                .filter(p -> t.isEmpty() || p.getTipo().equalsIgnoreCase(t))
                .toList();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public Producto crear(ProductoRequest datos) {
        if (datos == null) {
            throw new DatosInvalidosException("Los datos del producto son obligatorios");
        }
        String nombre = datos.getNombre() == null ? "" : datos.getNombre().trim();
        String tipo = datos.getTipo() == null ? "" : datos.getTipo().trim();
        if (nombre.isEmpty() || nombre.length() > 50) {
            throw new DatosInvalidosException("El nombre debe tener entre 1 y 50 caracteres");
        }
        if (tipo.isEmpty() || tipo.length() > 30) {
            throw new DatosInvalidosException("El tipo debe tener entre 1 y 30 caracteres");
        }
        if (datos.getStock() == null || datos.getStock() < 0 || datos.getStock() > MAX_STOCK) {
            throw new DatosInvalidosException("El stock debe estar entre 0 y " + MAX_STOCK);
        }
        if (datos.getPrecio() == null || !(datos.getPrecio() > 0) || Double.isInfinite(datos.getPrecio())) {
            throw new DatosInvalidosException("El precio debe ser mayor a cero");
        }
        boolean repetido = inventarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
        if (repetido) {
            throw new ReglaNegocioException("Ya existe un producto llamado " + nombre);
        }

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setTipo(tipo);
        producto.setStock(datos.getStock());
        producto.setPrecio(Math.round(datos.getPrecio() * 100.0) / 100.0);
        return inventarioRepository.save(producto);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public int registrarEntrada(Long idProducto, int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor a cero");
        }
        Producto producto = bloquear(idProducto);
        long nuevo = (long) producto.getStock() + cantidad;
        if (nuevo > MAX_STOCK) {
            throw new ReglaNegocioException("El stock no puede superar " + MAX_STOCK + " unidades");
        }
        producto.setStock((int) nuevo);
        return productoRepository.save(producto).getStock();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional
    public int registrarSalida(Long idProducto, int cantidad) {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad debe ser mayor a cero");
        }
        Producto producto = bloquear(idProducto);
        if (producto.getStock() < cantidad) {
            throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre()
                    + ". Disponible: " + producto.getStock() + ", solicitado: " + cantidad);
        }
        producto.setStock(producto.getStock() - cantidad);
        return productoRepository.save(producto).getStock();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional
    public int ajustarStock(Long idProducto, int nuevoStock) {
        if (nuevoStock < 0) {
            throw new DatosInvalidosException("El stock no puede ser negativo");
        }
        if (nuevoStock > MAX_STOCK) {
            throw new DatosInvalidosException("El stock no puede superar " + MAX_STOCK + " unidades");
        }
        Producto producto = bloquear(idProducto);
        producto.setStock(nuevoStock);
        return productoRepository.save(producto).getStock();
    }

    /** Bloqueo pesimista: evita perder movimientos si dos personas editan el mismo producto a la vez. */
    private Producto bloquear(Long idProducto) {
        return productoRepository.findByIdForUpdate(idProducto)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }
}
