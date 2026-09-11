package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.Paquete;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {
    @EntityGraph(attributePaths = "usuario")
    List<Paquete> findAllByOrderByIdPaqueteDesc();
}
