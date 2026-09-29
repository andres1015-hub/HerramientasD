package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.DetallePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DetallePackageRepository extends JpaRepository<DetallePackage,Long> {
    @Query("""
        SELECT d FROM DetallePackage d
        JOIN FETCH d.paquete p
        JOIN FETCH p.usuario
        JOIN FETCH d.producto
        WHERE d.codigoQR = :codigoQR
    """)
    Optional<DetallePackage> findByCodigoQRWithRelations(@Param("codigoQR") String codigoQR);
}
