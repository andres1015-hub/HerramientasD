package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventarioRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByStockLessThanEqual(int minimo);

    List<Producto> findByTipo(String tipo);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
