package com.example.ProyectoDesarrollo.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Product(
        Long id,
        String code,
        String name,
        String description,
        String category,
        BigDecimal price,
        int stock,
        int minimumStock,
        Status status,
        LocalDateTime createdAt
) {
    public enum Status {
        ACTIVO, INACTIVO
    }

    public boolean isLowStock() {
        return stock <= minimumStock;
    }
}
