package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Models.Product;
import com.example.ProyectoDesarrollo.Services.InventoryService;
import com.example.ProyectoDesarrollo.Services.QrCodeService;
import com.google.zxing.WriterException;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class QrController {

    private final InventoryService inventoryService;
    private final QrCodeService qrCodeService;

    public QrController(InventoryService inventoryService, QrCodeService qrCodeService) {
        this.inventoryService = inventoryService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/qr")
    public String qr(@RequestParam(name = "productoId", required = false) Long productId, Model model) {
        model.addAttribute("products", inventoryService.products());
        if (productId != null) {
            model.addAttribute("selectedProduct", inventoryService.product(productId));
        }
        return "qr";
    }

    @GetMapping(value = "/qr/productos/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> productQr(@PathVariable long id,
                                             @RequestParam(defaultValue = "false") boolean download)
            throws WriterException, IOException {
        Product product = inventoryService.product(id);
        String content = "INVENTARIO-QR|id=" + product.id() + "|codigo=" + product.code()
                + "|nombre=" + product.name();
        byte[] png = qrCodeService.generatePng(content, 320);
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename("qr-" + product.code() + ".png")
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(CacheControl.noStore())
                .body(png);
    }
}
