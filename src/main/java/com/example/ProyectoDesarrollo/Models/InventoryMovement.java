package com.example.ProyectoDesarrollo.Models;

import java.time.LocalDateTime;

public record InventoryMovement(
        Long id,
        Long productId,
        String productCode,
        String productName,
        Type type,
        int quantity,
        int resultingStock,
        String reference,
        LocalDateTime createdAt
) {
    public enum Type {
        ENTRADA, SALIDA
    }
}
