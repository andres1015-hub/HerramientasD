package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MovimientoStockRepository
        extends JpaRepository<MovimientoStock, Long>, JpaSpecificationExecutor<MovimientoStock> {
}
