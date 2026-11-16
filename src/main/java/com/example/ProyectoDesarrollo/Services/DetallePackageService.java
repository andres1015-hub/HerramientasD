package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.DetallePackageResponse;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Models.DetallePackage;
import com.example.ProyectoDesarrollo.Repositories.DetallePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DetallePackageService {

    private static final int MAX_CODIGO_QR = 100;

    private final DetallePackageRepository repo;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional(readOnly = true)
    public DetallePackageResponse obtenerPorQR(String codigoQR) {
        if (codigoQR == null || codigoQR.isBlank()) {
            throw new DatosInvalidosException("El codigo QR es obligatorio");
        }
        String codigo = codigoQR.trim();
        if (codigo.length() > MAX_CODIGO_QR) {
            throw new DatosInvalidosException("El codigo QR no puede superar los " + MAX_CODIGO_QR + " caracteres");
        }

        DetallePackage detalle = repo.findByCodigoQRWithRelations(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Codigo QR no valido"));
        return DetallePackageResponse.from(detalle);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional(readOnly = true)
    public DetallePackageResponse obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            throw new DatosInvalidosException("El identificador del detalle no es valido");
        }
        DetallePackage detalle = repo.findByIdWithRelations(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Detalle no encontrado"));
        return DetallePackageResponse.from(detalle);
    }
}
