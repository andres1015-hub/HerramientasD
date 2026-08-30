package com.example.ProyectoDesarrollo.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InventoryOrder(
        Long id,
        String code,
        String customer,
        Status status,
        List<Item> items,
        BigDecimal total,
        LocalDateTime createdAt
) {
    public enum Status {
        PENDIENTE, PROCESANDO, COMPLETADO, CANCELADO
    }

    public record Item(
            Long productId,
            String productCode,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
