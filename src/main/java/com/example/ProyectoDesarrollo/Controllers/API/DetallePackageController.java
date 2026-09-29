package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.DetallePackageResponse;
import com.example.ProyectoDesarrollo.DTO.DetalleQrRequest;
import com.example.ProyectoDesarrollo.Services.DetallePackageService;
import com.example.ProyectoDesarrollo.Services.QRService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detalle")
@RequiredArgsConstructor
public class DetallePackageController {

    private final DetallePackageService service;
    private final QRService qrService;

    @PostMapping("/qr")
    public ResponseEntity<DetallePackageResponse> escanearQR(
            @Valid @RequestBody DetalleQrRequest request) {
        return ResponseEntity.ok(service.obtenerPorQR(request.getCodigoQR()));
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> imagenQRPorId(@PathVariable Long id) {
        DetallePackageResponse dto = service.obtenerPorId(id);
        byte[] png = qrService.generarPNG(dto.getCodigoQR(), 300, 300);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}