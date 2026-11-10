package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.Paquete;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    List<Paquete> findAllByOrderByIdPaqueteDesc();

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.producto"})
    @Query("SELECT p FROM Paquete p WHERE p.idPaquete = :id")
    Optional<Paquete> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT COUNT(p) FROM Paquete p WHERE LOWER(p.nPedido) = LOWER(:pedido)")
    long contarPorPedido(@Param("pedido") String pedido);
}
