package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Models.InventoryMovement;
import com.example.ProyectoDesarrollo.Models.InventoryOrder;
import com.example.ProyectoDesarrollo.Models.Product;
import com.example.ProyectoDesarrollo.Services.InventoryService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InventoryApiController {
    private final InventoryService inventoryService;

    public InventoryApiController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/dashboard")
    public InventoryService.DashboardData dashboard() {
        return inventoryService.dashboard();
    }

    @GetMapping("/products")
    public List<Product> products() {
        return inventoryService.products();
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable long id) {
        return inventoryService.product(id);
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody InventoryService.ProductInput input) {
        return ResponseEntity.status(201).body(inventoryService.createProduct(input));
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable long id, @RequestBody InventoryService.ProductInput input) {
        return inventoryService.updateProduct(id, input);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id) {
        inventoryService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movements")
    public List<InventoryMovement> movements() {
        return inventoryService.movements();
    }

    @PostMapping("/movements")
    public ResponseEntity<InventoryMovement> addMovement(@RequestBody InventoryService.MovementInput input) {
        return ResponseEntity.status(201).body(inventoryService.addMovement(input));
    }

    @GetMapping("/orders")
    public List<InventoryOrder> orders() {
        return inventoryService.orders();
    }

    @GetMapping("/orders/{id}")
    public InventoryOrder order(@PathVariable long id) {
        return inventoryService.order(id);
    }

    @PostMapping("/orders")
    public ResponseEntity<InventoryOrder> createOrder(@RequestBody InventoryService.OrderInput input) {
        return ResponseEntity.status(201).body(inventoryService.createOrder(input));
    }

    @PatchMapping("/orders/{id}/status")
    public InventoryOrder updateOrderStatus(@PathVariable long id, @RequestBody Map<String, String> payload) {
        String value = payload.get("status");
        InventoryOrder.Status status;
        try {
            status = value == null ? null : InventoryOrder.Status.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Estado de pedido no válido");
        }
        return inventoryService.updateOrderStatus(id, status);
    }

    @GetMapping(value = "/products/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> productQr(@PathVariable long id,
                                             @RequestParam(defaultValue = "false") boolean download) throws Exception {
        Product product = inventoryService.product(id);
        String content = "QR-STOCK:" + product.id() + ":" + product.code();
        BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 360, 360);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", output);

        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename("qr-" + product.code() + ".png")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .body(output.toByteArray());
    }

    @PostMapping(value = "/qr/read", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product readQr(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Selecciona una imagen con un código QR");
        }
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("El archivo no es una imagen válida");
        }
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        Result decoded;
        try {
            decoded = new MultiFormatReader().decode(bitmap);
        } catch (com.google.zxing.NotFoundException exception) {
            throw new IllegalArgumentException("No se encontró un código QR legible en la imagen");
        }
        String[] parts = decoded.getText().split(":", 3);
        if (parts.length != 3 || !"QR-STOCK".equals(parts[0])) {
            throw new IllegalArgumentException("El código QR no pertenece a este inventario");
        }
        return inventoryService.productByCode(parts[2]);
    }
}
