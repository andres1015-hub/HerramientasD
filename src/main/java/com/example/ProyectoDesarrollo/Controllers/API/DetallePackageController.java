package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.DetallePackageResponse;
import com.example.ProyectoDesarrollo.DTO.DetalleQrRequest;
import com.example.ProyectoDesarrollo.Services.DetallePackageService;
import com.example.ProyectoDesarrollo.Services.QRService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/detalle")
@RequiredArgsConstructor
public class DetallePackageController {

    private final DetallePackageService service;
    private final QRService qrService;

    @PostMapping(value = "/qr", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    public ResponseEntity<DetallePackageResponse> escanearQR(
            @Valid @RequestBody DetalleQrRequest request, Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesi��n para escanear");
        }
        return ResponseEntity.ok(service.obtenerPorQR(request.getCodigoQR()));
    }

    /** Igual que POST /qr, para enlaces: acepta el código o la URL completa leída del QR. */
    @GetMapping("/qr")
    public DetallePackageResponse consultarQR(@RequestParam("codigo") String codigo) {
        return service.obtenerPorQR(codigo);
    }

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    public ResponseEntity<byte[]> imagenQRPorId(@PathVariable @Positive Long id, Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesi��n para ver el QR");
        }
        DetallePackageResponse dto = service.obtenerPorId(id);
        byte[] png = qrService.generarPNG(dto.getCodigoQR(), 300, 300);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
