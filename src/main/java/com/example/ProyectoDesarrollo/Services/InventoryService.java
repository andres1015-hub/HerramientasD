package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.Product;
import com.example.ProyectoDesarrollo.Models.ProductForm;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final Map<Long, Product> products = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @PostConstruct
    synchronized void seed() {
        if (!products.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        seedProduct("EQ-001", "Escáner QR portátil", "Lector inalámbrico para códigos QR.",
                "Equipos", "289.90", 18, 6, now.minusDays(18));
        seedProduct("IN-014", "Etiquetas térmicas 50x30", "Rollo de 500 etiquetas adhesivas.",
                "Insumos", "24.50", 4, 10, now.minusDays(15));
        seedProduct("EQ-008", "Impresora térmica", "Impresora de etiquetas para almacén.",
                "Equipos", "649.00", 7, 3, now.minusDays(12));
        seedProduct("AL-021", "Caja organizadora M", "Contenedor apilable para inventario.",
                "Almacenamiento", "38.90", 32, 8, now.minusDays(10));
        seedProduct("EQ-012", "Tablet industrial", "Terminal resistente para control de stock.",
                "Equipos", "1249.00", 3, 4, now.minusDays(8));
        seedProduct("IN-025", "Cinta de embalaje", "Cinta transparente reforzada de 48 mm.",
                "Insumos", "8.90", 56, 15, now.minusDays(6));
        seedProduct("AL-032", "Estante metálico", "Estantería modular de cinco niveles.",
                "Almacenamiento", "399.00", 11, 2, now.minusDays(4));
        seedProduct("SE-006", "Guantes de seguridad", "Guantes anticorte para manipulación.",
                "Seguridad", "42.00", 9, 12, now.minusDays(2));
    }

    public synchronized List<Product> products() {
        return products.values().stream()
                .sorted(Comparator.comparing(Product::createdAt).reversed())
                .toList();
    }

    public synchronized List<Product> search(String query) {
        if (query == null || query.isBlank()) {
            return products();
        }
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        return products().stream()
                .filter(product -> product.code().toLowerCase(Locale.ROOT).contains(normalized)
                        || product.name().toLowerCase(Locale.ROOT).contains(normalized)
                        || product.category().toLowerCase(Locale.ROOT).contains(normalized))
                .toList();
    }

    public synchronized Product product(long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new NoSuchElementException("Producto no encontrado");
        }
        return product;
    }

    public synchronized Product create(ProductForm form) {
        validateUniqueCode(form.getCode(), null);
        long id = sequence.incrementAndGet();
        Product product = toProduct(id, form, LocalDateTime.now(), Product.Status.ACTIVO);
        products.put(id, product);
        return product;
    }

    public synchronized Product update(long id, ProductForm form) {
        Product current = product(id);
        validateUniqueCode(form.getCode(), id);
        Product updated = toProduct(id, form, current.createdAt(), current.status());
        products.put(id, updated);
        return updated;
    }

    public synchronized void delete(long id) {
        product(id);
        products.remove(id);
    }

    public synchronized DashboardData dashboard() {
        List<Product> allProducts = products();
        int totalStock = allProducts.stream().mapToInt(Product::stock).sum();
        long lowStock = allProducts.stream().filter(Product::isLowStock).count();
        return new DashboardData(allProducts.size(), totalStock, lowStock, allProducts.stream().limit(5).toList());
    }

    public synchronized List<CategorySummary> categorySummary() {
        Map<String, List<Product>> byCategory = products().stream()
                .collect(Collectors.groupingBy(Product::category, TreeMap::new, Collectors.toList()));
        return byCategory.entrySet().stream()
                .map(entry -> new CategorySummary(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(Product::stock).sum()))
                .toList();
    }

    private Product toProduct(long id, ProductForm form, LocalDateTime createdAt, Product.Status status) {
        return new Product(
                id,
                form.getCode().trim().toUpperCase(Locale.ROOT),
                form.getName().trim(),
                form.getDescription() == null ? "" : form.getDescription().trim(),
                normalizeCategory(form.getCategory()),
                form.getPrice().setScale(2, RoundingMode.HALF_UP),
                form.getStock(),
                form.getMinimumStock(),
                status,
                createdAt);
    }

    private void validateUniqueCode(String code, Long currentId) {
        String normalized = code.trim();
        boolean duplicated = products.values().stream()
                .anyMatch(product -> !product.id().equals(currentId)
                        && product.code().equalsIgnoreCase(normalized));
        if (duplicated) {
            throw new IllegalArgumentException("Ya existe un producto con ese código");
        }
    }

    private String normalizeCategory(String category) {
        String normalized = category.trim();
        return products.values().stream()
                .map(Product::category)
                .filter(existing -> existing.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(normalized);
    }

    private void seedProduct(String code, String name, String description, String category, String price,
                             int stock, int minimumStock, LocalDateTime createdAt) {
        long id = sequence.incrementAndGet();
        products.put(id, new Product(id, code, name, description, category, new BigDecimal(price), stock,
                minimumStock, Product.Status.ACTIVO, createdAt));
    }

    public record DashboardData(int totalProducts, int totalStock, long lowStockProducts,
                                List<Product> recentProducts) {
    }

    public record CategorySummary(String category, int products, int units) {
    }
}
