package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.InventoryMovement;
import com.example.ProyectoDesarrollo.Models.InventoryOrder;
import com.example.ProyectoDesarrollo.Models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryServiceTests {
    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService();
        service.seed();
    }

    @Test
    void createsUpdatesAndDeletesProducts() {
        Product created = service.createProduct(new InventoryService.ProductInput(
                "te-999", "Producto de prueba", "Descripción", "Pruebas",
                new BigDecimal("10.50"), 5, 2, Product.Status.ACTIVO));

        assertThat(created.code()).isEqualTo("TE-999");
        assertThat(service.products()).contains(created);

        Product updated = service.updateProduct(created.id(), new InventoryService.ProductInput(
                "TE-999", "Producto actualizado", "Descripción", "Pruebas",
                new BigDecimal("12.00"), 8, 2, Product.Status.ACTIVO));

        assertThat(updated.name()).isEqualTo("Producto actualizado");
        assertThat(service.movements().getFirst().reference()).isEqualTo("Ajuste de producto");

        service.deleteProduct(created.id());
        assertThatThrownBy(() -> service.product(created.id())).isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void rejectsMovementsAndOrdersWithoutEnoughStock() {
        Product lowStockProduct = service.products().stream()
                .min(java.util.Comparator.comparingInt(Product::stock))
                .orElseThrow();

        assertThatThrownBy(() -> service.addMovement(new InventoryService.MovementInput(
                lowStockProduct.id(), InventoryMovement.Type.SALIDA, lowStockProduct.stock() + 1, "Prueba")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supera");

        assertThatThrownBy(() -> service.createOrder(new InventoryService.OrderInput(
                "Cliente", InventoryOrder.Status.PENDIENTE,
                List.of(new InventoryService.OrderItemInput(lowStockProduct.id(), lowStockProduct.stock() + 1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("insuficiente");
    }

    @Test
    void createsOrderAndDeductsStock() {
        Product product = service.products().stream().filter(item -> item.stock() >= 2).findFirst().orElseThrow();
        int previousStock = product.stock();

        InventoryOrder order = service.createOrder(new InventoryService.OrderInput(
                "Cliente API", InventoryOrder.Status.PENDIENTE,
                List.of(new InventoryService.OrderItemInput(product.id(), 2))));

        assertThat(order.code()).startsWith("PED-");
        assertThat(order.items()).hasSize(1);
        assertThat(service.product(product.id()).stock()).isEqualTo(previousStock - 2);
        assertThat(service.movements().getFirst().reference()).isEqualTo(order.code());
    }
}
