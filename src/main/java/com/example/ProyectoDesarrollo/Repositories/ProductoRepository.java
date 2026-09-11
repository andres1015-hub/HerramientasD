package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdProductoNot(String codigo, long idProducto);

    @Query("select distinct p.tipo from Producto p order by p.tipo")
    List<String> categories();

    @Query("select count(d) > 0 from DetallePackage d where d.producto.idProducto = :id")
    boolean hasPackageDetails(@Param("id") long id);
}
