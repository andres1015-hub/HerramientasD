package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.InventoryMovement;
import com.example.ProyectoDesarrollo.Models.InventoryOrder;
import com.example.ProyectoDesarrollo.Models.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InventoryService {
    private final Map<Long, Product> products = new LinkedHashMap<>();
    private final Map<Long, InventoryOrder> orders = new LinkedHashMap<>();
    private final Map<Long, InventoryMovement> movements = new LinkedHashMap<>();
    private final AtomicLong productSequence = new AtomicLong();
    private final AtomicLong orderSequence = new AtomicLong();
    private final AtomicLong movementSequence = new AtomicLong();

    @PostConstruct
    synchronized void seed() {
        if (!products.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Product scanner = seedProduct("EQ-001", "Escáner QR portátil", "Lector inalámbrico de códigos QR y barras.", "Equipos", "289.90", 18, 6, now.minusDays(18));
        Product labels = seedProduct("IN-014", "Etiquetas térmicas 50x30", "Rollo de 500 etiquetas adhesivas.", "Insumos", "24.50", 4, 10, now.minusDays(15));
        Product printer = seedProduct("EQ-008", "Impresora térmica", "Impresora de etiquetas de alta velocidad.", "Equipos", "649.00", 7, 3, now.minusDays(12));
        Product box = seedProduct("AL-021", "Caja organizadora M", "Contenedor apilable de almacén.", "Almacenamiento", "38.90", 32, 8, now.minusDays(10));
        Product tablet = seedProduct("EQ-012", "Tablet industrial", "Terminal resistente para gestión en almacén.", "Equipos", "1249.00", 3, 4, now.minusDays(8));
        Product tape = seedProduct("IN-025", "Cinta de embalaje", "Cinta transparente reforzada de 48 mm.", "Insumos", "8.90", 56, 15, now.minusDays(6));
        Product shelf = seedProduct("AL-032", "Estante metálico", "Estantería modular de cinco niveles.", "Almacenamiento", "399.00", 11, 2, now.minusDays(4));
        Product gloves = seedProduct("SE-006", "Guantes de seguridad", "Par de guantes anticorte nivel 5.", "Seguridad", "42.00", 9, 12, now.minusDays(2));

        seedMovement(scanner, InventoryMovement.Type.ENTRADA, 20, 18, "Compra inicial", now.minusDays(9));
        seedMovement(labels, InventoryMovement.Type.SALIDA, 6, 4, "Consumo interno", now.minusDays(6));
        seedMovement(printer, InventoryMovement.Type.ENTRADA, 3, 7, "OC-1042", now.minusDays(4));
        seedMovement(tablet, InventoryMovement.Type.SALIDA, 2, 3, "PED-1002", now.minusDays(3));
        seedMovement(tape, InventoryMovement.Type.ENTRADA, 24, 56, "Reposición", now.minusDays(2));
        seedMovement(gloves, InventoryMovement.Type.SALIDA, 3, 9, "PED-1004", now.minusHours(8));

        seedOrder("PED-1001", "Constructora Andina", InventoryOrder.Status.COMPLETADO,
                List.of(item(scanner, 2), item(labels, 4)), now.minusDays(8));
        seedOrder("PED-1002", "Logística Norte", InventoryOrder.Status.PROCESANDO,
                List.of(item(tablet, 2), item(box, 6)), now.minusDays(3));
        seedOrder("PED-1003", "Mercado Central", InventoryOrder.Status.PENDIENTE,
                List.of(item(tape, 10)), now.minusDays(1));
        seedOrder("PED-1004", "Operaciones Sur", InventoryOrder.Status.PENDIENTE,
                List.of(item(gloves, 3), item(labels, 2)), now.minusHours(8));
    }

    public synchronized List<Product> products() {
        return products.values().stream()
                .sorted(Comparator.comparing(Product::createdAt).reversed())
                .toList();
    }

    public synchronized Product product(long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new NoSuchElementException("Producto no encontrado");
        }
        return product;
    }

    public synchronized Product productByCode(String code) {
        return products.values().stream()
                .filter(product -> product.code().equalsIgnoreCase(normalize(code)))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe un producto asociado a este código QR"));
    }

    public synchronized Product createProduct(ProductInput input) {
        validateProduct(input, null);
        long id = productSequence.incrementAndGet();
        Product product = new Product(id, normalize(input.code()), input.name().trim(), text(input.description()),
                input.category().trim(), money(input.price()), input.stock(), input.minimumStock(),
                input.status() == null ? Product.Status.ACTIVO : input.status(), LocalDateTime.now());
        products.put(id, product);
        if (product.stock() > 0) {
            recordMovement(product, InventoryMovement.Type.ENTRADA, product.stock(), product.stock(), "Stock inicial");
        }
        return product;
    }

    public synchronized Product updateProduct(long id, ProductInput input) {
        Product current = product(id);
        validateProduct(input, id);
        Product updated = new Product(id, normalize(input.code()), input.name().trim(), text(input.description()),
                input.category().trim(), money(input.price()), input.stock(), input.minimumStock(),
                input.status() == null ? Product.Status.ACTIVO : input.status(), current.createdAt());
        products.put(id, updated);

        int difference = updated.stock() - current.stock();
        if (difference != 0) {
            recordMovement(updated, difference > 0 ? InventoryMovement.Type.ENTRADA : InventoryMovement.Type.SALIDA,
                    Math.abs(difference), updated.stock(), "Ajuste de producto");
        }
        return updated;
    }

    public synchronized void deleteProduct(long id) {
        product(id);
        products.remove(id);
    }

    public synchronized List<InventoryMovement> movements() {
        return movements.values().stream()
                .sorted(Comparator.comparing(InventoryMovement::createdAt).reversed())
                .toList();
    }

    public synchronized InventoryMovement addMovement(MovementInput input) {
        if (input.quantity() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (input.type() == null) {
            throw new IllegalArgumentException("Selecciona el tipo de movimiento");
        }
        Product current = product(input.productId());
        int resultingStock = input.type() == InventoryMovement.Type.ENTRADA
                ? current.stock() + input.quantity()
                : current.stock() - input.quantity();
        if (resultingStock < 0) {
            throw new IllegalArgumentException("La salida supera el stock disponible");
        }
        Product updated = new Product(current.id(), current.code(), current.name(), current.description(),
                current.category(), current.price(), resultingStock, current.minimumStock(), current.status(), current.createdAt());
        products.put(current.id(), updated);
        return recordMovement(updated, input.type(), input.quantity(), resultingStock, textOr(input.reference(), "Movimiento manual"));
    }

    public synchronized List<InventoryOrder> orders() {
        return orders.values().stream()
                .sorted(Comparator.comparing(InventoryOrder::createdAt).reversed())
                .toList();
    }

    public synchronized InventoryOrder order(long id) {
        InventoryOrder order = orders.get(id);
        if (order == null) {
            throw new NoSuchElementException("Pedido no encontrado");
        }
        return order;
    }

    public synchronized InventoryOrder createOrder(OrderInput input) {
        if (input.customer() == null || input.customer().isBlank()) {
            throw new IllegalArgumentException("Ingresa el cliente o solicitante");
        }
        if (input.items() == null || input.items().isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un producto al pedido");
        }

        Map<Long, Integer> requested = new LinkedHashMap<>();
        for (OrderItemInput line : input.items()) {
            if (line.quantity() <= 0) {
                throw new IllegalArgumentException("Todas las cantidades deben ser mayores que cero");
            }
            requested.merge(line.productId(), line.quantity(), Integer::sum);
        }

        List<InventoryOrder.Item> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : requested.entrySet()) {
            Product product = product(entry.getKey());
            if (product.status() == Product.Status.INACTIVO) {
                throw new IllegalArgumentException(product.name() + " está inactivo");
            }
            if (product.stock() < entry.getValue()) {
                throw new IllegalArgumentException("Stock insuficiente para " + product.name());
            }
            lines.add(item(product, entry.getValue()));
        }

        long id = orderSequence.incrementAndGet();
        String code = "PED-" + (1000 + id);
        InventoryOrder.Status status = input.status() == null ? InventoryOrder.Status.PENDIENTE : input.status();
        BigDecimal total = lines.stream().map(InventoryOrder.Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        InventoryOrder order = new InventoryOrder(id, code, input.customer().trim(), status, List.copyOf(lines), total, LocalDateTime.now());
        orders.put(id, order);

        if (status != InventoryOrder.Status.CANCELADO) {
            for (InventoryOrder.Item line : lines) {
                Product current = product(line.productId());
                int resultingStock = current.stock() - line.quantity();
                Product updated = new Product(current.id(), current.code(), current.name(), current.description(),
                        current.category(), current.price(), resultingStock, current.minimumStock(), current.status(), current.createdAt());
                products.put(current.id(), updated);
                recordMovement(updated, InventoryMovement.Type.SALIDA, line.quantity(), resultingStock, code);
            }
        }
        return order;
    }

    public synchronized InventoryOrder updateOrderStatus(long id, InventoryOrder.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Selecciona un estado válido");
        }
        InventoryOrder current = order(id);
        InventoryOrder updated = new InventoryOrder(current.id(), current.code(), current.customer(), status,
                current.items(), current.total(), current.createdAt());
        orders.put(id, updated);
        return updated;
    }

    public synchronized DashboardData dashboard() {
        List<Product> productList = products();
        List<InventoryOrder> orderList = orders();
        List<InventoryMovement> movementList = movements();
        int totalStock = productList.stream().mapToInt(Product::stock).sum();
        List<Product> lowStock = productList.stream().filter(Product::isLowStock).toList();
        long openOrders = orderList.stream()
                .filter(order -> order.status() == InventoryOrder.Status.PENDIENTE || order.status() == InventoryOrder.Status.PROCESANDO)
                .count();
        return new DashboardData(productList.size(), totalStock, lowStock.size(), orderList.size(), openOrders,
                movementList.stream().limit(6).toList(), productList.stream().limit(5).toList(),
                orderList.stream().limit(5).toList(), lowStock);
    }

    private Product seedProduct(String code, String name, String description, String category, String price,
                                int stock, int minimumStock, LocalDateTime createdAt) {
        long id = productSequence.incrementAndGet();
        Product product = new Product(id, code, name, description, category, new BigDecimal(price), stock,
                minimumStock, Product.Status.ACTIVO, createdAt);
        products.put(id, product);
        return product;
    }

    private void seedMovement(Product product, InventoryMovement.Type type, int quantity, int resultingStock,
                              String reference, LocalDateTime createdAt) {
        long id = movementSequence.incrementAndGet();
        movements.put(id, new InventoryMovement(id, product.id(), product.code(), product.name(), type,
                quantity, resultingStock, reference, createdAt));
    }

    private void seedOrder(String code, String customer, InventoryOrder.Status status, List<InventoryOrder.Item> items,
                           LocalDateTime createdAt) {
        long id = orderSequence.incrementAndGet();
        BigDecimal total = items.stream().map(InventoryOrder.Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        orders.put(id, new InventoryOrder(id, code, customer, status, items, total, createdAt));
    }

    private InventoryOrder.Item item(Product product, int quantity) {
        return new InventoryOrder.Item(product.id(), product.code(), product.name(), quantity, product.price(),
                product.price().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
    }

    private InventoryMovement recordMovement(Product product, InventoryMovement.Type type, int quantity,
                                             int resultingStock, String reference) {
        long id = movementSequence.incrementAndGet();
        InventoryMovement movement = new InventoryMovement(id, product.id(), product.code(), product.name(), type,
                quantity, resultingStock, reference, LocalDateTime.now());
        movements.put(id, movement);
        return movement;
    }

    private void validateProduct(ProductInput input, Long currentId) {
        if (input == null || input.code() == null || input.code().isBlank()) {
            throw new IllegalArgumentException("Ingresa el código del producto");
        }
        if (input.name() == null || input.name().isBlank()) {
            throw new IllegalArgumentException("Ingresa el nombre del producto");
        }
        if (input.category() == null || input.category().isBlank()) {
            throw new IllegalArgumentException("Ingresa la categoría");
        }
        if (input.price() == null || input.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (input.stock() < 0 || input.minimumStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        boolean duplicated = products.values().stream()
                .anyMatch(product -> !product.id().equals(currentId) && product.code().equalsIgnoreCase(input.code().trim()));
        if (duplicated) {
            throw new IllegalArgumentException("Ya existe un producto con ese código");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private String textOr(String value, String fallback) {
        String normalized = text(value);
        return normalized.isBlank() ? fallback : normalized;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record ProductInput(String code, String name, String description, String category, BigDecimal price,
                               int stock, int minimumStock, Product.Status status) {
    }

    public record MovementInput(long productId, InventoryMovement.Type type, int quantity, String reference) {
    }

    public record OrderItemInput(long productId, int quantity) {
    }

    public record OrderInput(String customer, InventoryOrder.Status status, List<OrderItemInput> items) {
    }

    public record DashboardData(int totalProducts, int totalStock, int lowStockProducts, int totalOrders,
                                long openOrders, List<InventoryMovement> recentMovements, List<Product> recentProducts,
                                List<InventoryOrder> recentOrders, List<Product> lowStockAlerts) {
    }
}
