package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.example.ProyectoDesarrollo.Models.Product;
import com.example.ProyectoDesarrollo.Models.ProductForm;
import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.ProductoRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:inventory-integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
@Transactional
class InventoryServiceTests {

    @Autowired
    private InventoryService service;

    @Autowired
    private ProductoRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Validator validator;

    @Test
    void startsWithNoInventedInventory() {
        assertEquals(0, service.dashboard().totalProducts());
        assertEquals(0, service.dashboard().totalStock());
        assertTrue(service.products().isEmpty());
    }

    @Test
    void readsExistingSchemaRowsWithoutRequiringOrWritingNewMetadata() {
        Producto original = existingProduct("Producto del compañero", 7);
        entityManager.flush();
        entityManager.clear();

        Product product = service.product(original.getIdProducto());
        assertEquals("AUTO-" + original.getIdProducto(), product.code());
        assertEquals("Producto del compañero", product.name());
        assertEquals(7, product.stock());
        assertEquals(0, product.minimumStock());
        assertEquals(Product.Status.ACTIVO, product.status());
        assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0), product.createdAt());
        assertEquals(new BigDecimal("19.75"), product.price());
        assertNull(jdbc.queryForObject("select codigo from producto where id_producto = ?", String.class,
                original.getIdProducto()));
        assertEquals(1, service.search(product.code()).size());
    }

    @Test
    void persistsCreatesUpdatesAndDeletesInTheOriginalTable() {
        Product created = service.create(form("pr-100", "Producto de prueba", 5, 2));
        entityManager.clear();

        assertEquals("PR-100", service.product(created.id()).code());
        assertEquals(5, jdbc.queryForObject("select stock from producto where id_producto = ?", Integer.class,
                created.id()));

        Product updated = service.update(created.id(), form("PR-100", "Producto actualizado", 1, 2));
        entityManager.clear();
        assertEquals(created.createdAt(), updated.createdAt());
        assertEquals("Producto actualizado", service.product(created.id()).name());
        assertTrue(service.product(created.id()).isLowStock());

        service.delete(created.id());
        entityManager.clear();
        assertFalse(repository.existsById(created.id()));
        assertThrows(NoSuchElementException.class, () -> service.product(created.id()));
    }

    @Test
    void preservesLegacyIdentifiersAndAllowsEditingWithoutInventingStoredCodes() {
        Producto original = existingProduct("Registro heredado", 8);
        entityManager.flush();
        entityManager.clear();

        ProductForm edit = ProductForm.from(service.product(original.getIdProducto()));
        edit.setStock(12);
        service.update(original.getIdProducto(), edit);
        entityManager.clear();

        Producto saved = repository.findById(original.getIdProducto()).orElseThrow();
        assertEquals(12, saved.getStock());
        assertNull(saved.getCodigo());
        assertNull(saved.getCreadoEn());
        assertEquals(original.getIdProducto(), saved.getIdProducto());
    }

    @Test
    void searchesByCodeNameAndCategoryIgnoringCaseAndNormalizesCategories() {
        ProductForm first = form("EQ-001", "Escáner QR", 4, 1);
        first.setCategory("Equipos");
        service.create(first);
        ProductForm second = form("EQ-002", "Tablet", 2, 3);
        second.setCategory("equipos");
        service.create(second);

        assertEquals(1, service.search("tablet").size());
        assertEquals(2, service.search("EQUIPOS").size());
        assertEquals("EQ-001", service.search("eq-001").getFirst().code());
        assertEquals(1, service.categorySummary().size());
        assertEquals(6, service.dashboard().totalStock());
        assertEquals(1, service.dashboard().lowStockProducts());
    }

    @Test
    void rejectsDuplicateCodesAndReservedAutomaticCodes() {
        service.create(form("PR-100", "Original", 5, 2));
        assertThrows(IllegalArgumentException.class,
                () -> service.create(form(" pr-100 ", "Duplicado", 4, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> service.create(form("AUTO-123", "Código reservado", 4, 1)));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void databaseUniquenessProtectsConcurrentSubmissions() throws Exception {
        var workers = Executors.newFixedThreadPool(6);
        CountDownLatch ready = new CountDownLatch(6);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> attempts = new ArrayList<>();
        try {
            for (int index = 0; index < 6; index++) {
                attempts.add(workers.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("No se iniciaron las solicitudes concurrentes");
                    }
                    try {
                        service.create(form("RACE-001", "Registro concurrente", 1, 0));
                        return true;
                    } catch (IllegalArgumentException expectedDuplicate) {
                        return false;
                    }
                }));
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            int successful = 0;
            for (Future<Boolean> attempt : attempts) {
                if (attempt.get(20, TimeUnit.SECONDS)) {
                    successful++;
                }
            }
            assertEquals(1, successful);
            assertEquals(1, service.search("RACE-001").size());
        } finally {
            start.countDown();
            workers.shutdownNow();
            workers.awaitTermination(10, TimeUnit.SECONDS);
            service.search("RACE-001").forEach(product -> service.delete(product.id()));
        }
    }

    @Test
    void refusesToDeleteProductsReferencedByExistingPackages() {
        Producto original = existingProduct("Producto relacionado", 10);
        Usuario user = new Usuario(0, "Prueba", 87654321, "Usuario", "test-only", List.of());
        entityManager.persist(user);
        Paquete pack = new Paquete(0, "PED-TEST", Paquete.Estado.Enviado, user, List.of());
        entityManager.persist(pack);
        DetallePackage detail = new DetallePackage(0, pack, original, 2);
        entityManager.persist(detail);
        entityManager.flush();
        entityManager.clear();

        assertThrows(IllegalStateException.class, () -> service.delete(original.getIdProducto()));
        assertEquals(10, service.product(original.getIdProducto()).stock());
        assertEquals(1, jdbc.queryForObject(
                "select count(*) from detalle_paquete where id_producto = ?", Integer.class,
                original.getIdProducto()));
    }

    @Test
    void validatesTheOriginalDatabaseFieldLimitsAndPricePrecision() {
        ProductForm invalid = form("TEST", "N".repeat(51), 1, 0);
        invalid.setCategory("C".repeat(31));
        invalid.setPrice(new BigDecimal("1000000000000.001"));

        var invalidFields = validator.validate(invalid).stream()
                .map(violation -> violation.getPropertyPath().toString()).toList();
        assertTrue(invalidFields.contains("name"));
        assertTrue(invalidFields.contains("category"));
        assertTrue(invalidFields.contains("price"));
    }

    private Producto existingProduct(String name, int stock) {
        Producto original = new Producto(0, name, stock, "Existentes", 19.75, new ArrayList<>());
        entityManager.persist(original);
        return original;
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
