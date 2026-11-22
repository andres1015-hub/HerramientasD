package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventarioRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByStockLessThanEqual(int minimo);

    List<Producto> findByTipo(String tipo);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByActivoTrue();

    List<Producto> findByStockLessThanEqualAndActivoTrue(int minimo);

    List<Producto> findByTipoAndActivoTrue(String tipo);

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    @Query("SELECT p FROM Producto p Where p.activo = true AND p.stock <= p.stockMinimo ORDER BY p.stock ASC, p" +
            ".nombre ASC")
    List<Producto>findAlertasStockBajo();

    @Query("SELECT DISTINCT p.tipo FROM Producto p WHERE p.activo = true ORDER BY p.tipo")
    List<String> findTiposActivos();
}
