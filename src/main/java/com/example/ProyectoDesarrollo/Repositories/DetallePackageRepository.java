package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Models.Paquete;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface DetallePackageRepository extends JpaRepository<DetallePackage, Long> {

    @Query("""
        SELECT d FROM DetallePackage d
        JOIN FETCH d.paquete p
        JOIN FETCH p.usuario
        JOIN FETCH d.producto
        WHERE d.codigoQR = :codigoQR
    """)
    Optional<DetallePackage> findByCodigoQRWithRelations(@Param("codigoQR") String codigoQR);

    @Query("""
        SELECT d FROM DetallePackage d
        JOIN FETCH d.paquete p
        JOIN FETCH p.usuario
        JOIN FETCH d.producto
        WHERE d.idDetalles = :id
    """)
    Optional<DetallePackage> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(d.cantidad * d.precioUnitario),0.0) FROM DetallePackage d"
    +"WHERE d.paquete.estado <> :excluido")
    Double sumarMontoExcluyendo(@Param("excluido")Paquete.Estado excluido);

    @Query("SELECT d.producto.idProducto, d.nombreProducto, SUM(d.cantidad) FROM DetallePackage d"
            +"WHERE d.paquete.estado <> :excluido"
            +"GROUP BY d.producto.idProducto, d.nombreProducto ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> topProductos(@Param("excluido") Paquete.Estado excluido, Pageable pageable);
}