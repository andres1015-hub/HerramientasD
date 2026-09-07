package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.Product;
import com.example.ProyectoDesarrollo.Models.ProductForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryServiceTests {

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService();
        service.seed();
    }

    @Test
    void seedsAcademicDashboardData() {
        InventoryService.DashboardData dashboard = service.dashboard();

        assertEquals(8, dashboard.totalProducts());
        assertEquals(140, dashboard.totalStock());
        assertEquals(3, dashboard.lowStockProducts());
        assertEquals(5, dashboard.recentProducts().size());
    }

    @Test
    void searchesByCodeNameOrCategoryIgnoringCase() {
        assertEquals(1, service.search("tablet").size());
        assertEquals(3, service.search("EQUIPOS").size());
        assertEquals("IN-014", service.search("in-014").getFirst().code());
    }

    @Test
    void createsUpdatesAndDeletesAProduct() {
        Product created = service.create(form("PR-100", "Producto de prueba", 5, 2));
        assertEquals("PR-100", created.code());

        ProductForm update = form("PR-100", "Producto actualizado", 1, 2);
        Product updated = service.update(created.id(), update);
        assertEquals("Producto actualizado", updated.name());
        assertTrue(updated.isLowStock());

        service.delete(created.id());
        assertThrows(NoSuchElementException.class, () -> service.product(created.id()));
    }

    @Test
    void rejectsDuplicateCodes() {
        assertThrows(IllegalArgumentException.class, () -> service.create(form("eq-001", "Duplicado", 4, 1)));
    }

    private ProductForm form(String code, String name, int stock, int minimumStock) {
        ProductForm form = new ProductForm();
        form.setCode(code);
        form.setName(name);
        form.setDescription("Registro utilizado por la prueba");
        form.setCategory("Pruebas");
        form.setPrice(new BigDecimal("10.50"));
        form.setStock(stock);
        form.setMinimumStock(minimumStock);
        return form;
    }
}
