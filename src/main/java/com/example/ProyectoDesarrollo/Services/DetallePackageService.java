package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.DetallePackageResponse;
import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Repositories.DetallePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DetallePackageService {

    private final DetallePackageRepository repo;

    @Transactional(readOnly = true)
    public DetallePackageResponse obtenerPorQR(String codigoQR) {
        DetallePackage detalle = repo.findByCodigoQRWithRelations(codigoQR)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Código QR no válido"));
        return DetallePackageResponse.from(detalle);
    }

    @Transactional(readOnly = true)
    public DetallePackageResponse obtenerPorId(Long id) {
        DetallePackage detalle = repo.findByIdWithRelations(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Detalle no encontrado"));
        return DetallePackageResponse.from(detalle);
    }
}