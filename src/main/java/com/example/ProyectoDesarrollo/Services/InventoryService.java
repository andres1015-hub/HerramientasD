package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.Product;
import com.example.ProyectoDesarrollo.Models.ProductForm;
import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Repositories.ProductoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private static final String LEGACY_CODE_PREFIX = "AUTO-";
    private static final LocalDateTime UNKNOWN_CREATED_AT = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final String REFERENCED_PRODUCT_MESSAGE =
            "No se puede eliminar un producto asociado a un paquete. Se conservaron sus datos.";

    private final ProductoRepository repository;

    public InventoryService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Product> products() {
        return repository.findAll().stream()
                .map(this::toProduct)
                .sorted(Comparator.comparing(Product::createdAt).thenComparing(Product::id).reversed())
                .toList();
    }

    public List<Product> search(String query) {
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

    public Product product(long id) {
        return toProduct(findEntity(id));
    }

    @Transactional
    public Product create(ProductForm form) {
        String code = normalizeCode(form.getCode(), null);
        validateUniqueCode(code, null);
        Producto entity = new Producto();
        applyForm(entity, form, code);
        entity.setEstado(Product.Status.ACTIVO);
        entity.setCreadoEn(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
        return save(entity);
    }

    @Transactional
    public Product update(long id, ProductForm form) {
        Producto entity = findEntity(id);
        String code = normalizeCode(form.getCode(), entity);
        validateUniqueCode(code, id);
        applyForm(entity, form, code);
        return save(entity);
    }

    @Transactional
    public void delete(long id) {
        Producto entity = findEntity(id);
        if (repository.hasPackageDetails(id)) {
            throw new IllegalStateException(REFERENCED_PRODUCT_MESSAGE);
        }
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            // The foreign key also protects a detail inserted after the check above.
            throw new IllegalStateException(REFERENCED_PRODUCT_MESSAGE, exception);
        }
    }

    public DashboardData dashboard() {
        List<Product> allProducts = products();
        int totalStock = allProducts.stream().mapToInt(Product::stock).sum();
        long lowStock = allProducts.stream().filter(Product::isLowStock).count();
        return new DashboardData(allProducts.size(), totalStock, lowStock, allProducts.stream().limit(5).toList());
    }

    public List<CategorySummary> categorySummary() {
        Map<String, List<Product>> byCategory = products().stream()
                .collect(Collectors.groupingBy(Product::category, TreeMap::new, Collectors.toList()));
        return byCategory.entrySet().stream()
                .map(entry -> new CategorySummary(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(Product::stock).sum()))
                .toList();
    }

    private Producto findEntity(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
    }

    private Product save(Producto entity) {
        try {
            return toProduct(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            // Flush here so concurrent duplicate codes become a form error.
            throw new IllegalArgumentException("No se pudo guardar el producto. Comprueba que el código no esté registrado.", exception);
        }
    }

    private Product toProduct(Producto entity) {
        return new Product(
                entity.getIdProducto(),
                entity.getCodigo() == null ? legacyCode(entity.getIdProducto()) : entity.getCodigo(),
                entity.getNombre(),
                entity.getDescripcion() == null ? "" : entity.getDescripcion(),
                entity.getTipo(),
                BigDecimal.valueOf(entity.getPrecio()).setScale(2, RoundingMode.HALF_UP),
                entity.getStock(),
                entity.getStockMinimo() == null ? 0 : entity.getStockMinimo(),
                entity.getEstado() == null ? Product.Status.ACTIVO : entity.getEstado(),
                entity.getCreadoEn() == null ? UNKNOWN_CREATED_AT : entity.getCreadoEn());
    }

    private void applyForm(Producto entity, ProductForm form, String code) {
        entity.setCodigo(code);
        entity.setNombre(form.getName().trim());
        entity.setDescripcion(form.getDescription() == null ? "" : form.getDescription().trim());
        entity.setTipo(normalizeCategory(form.getCategory()));
        // Retain the teammate's existing precio column and its double mapping.
        entity.setPrecio(form.getPrice().setScale(2, RoundingMode.HALF_UP).doubleValue());
        entity.setStock(form.getStock());
        entity.setStockMinimo(form.getMinimumStock());
    }

    private String normalizeCode(String code, Producto current) {
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        if (current != null && current.getCodigo() == null
                && normalized.equals(legacyCode(current.getIdProducto()))) {
            return null;
        }
        if (normalized.startsWith(LEGACY_CODE_PREFIX)) {
            throw new IllegalArgumentException("El prefijo AUTO- está reservado para productos existentes sin código.");
        }
        return normalized;
    }

    private String legacyCode(long id) {
        return LEGACY_CODE_PREFIX + id;
    }

    private void validateUniqueCode(String code, Long currentId) {
        if (code == null) {
            return;
        }
        boolean duplicated = currentId == null
                ? repository.existsByCodigoIgnoreCase(code)
                : repository.existsByCodigoIgnoreCaseAndIdProductoNot(code, currentId);
        if (duplicated) {
            throw new IllegalArgumentException("Ya existe un producto con ese código");
        }
    }

    private String normalizeCategory(String category) {
        String normalized = category.trim();
        return repository.categories().stream()
                .filter(existing -> existing.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(normalized);
    }

    public record DashboardData(int totalProducts, int totalStock, long lowStockProducts,
                                List<Product> recentProducts) {
    }

    public record CategorySummary(String category, int products, int units) {
    }
}
