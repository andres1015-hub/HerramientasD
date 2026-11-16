package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.DTO.DetallePackageResponse;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
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
    private final QRService qrService;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    @Transactional(readOnly = true)
    public DetallePackageResponse obtenerPorQR(String contenidoQR) {
        String codigoQR = qrService.extraerCodigo(contenidoQR);
        if (codigoQR.isEmpty()) {
            throw new DatosInvalidosException("El código está vacío. Inténtalo de nuevo.");
        }
        if (codigoQR.length() > QRService.LONGITUD_MAXIMA_CODIGO) {
            throw new DatosInvalidosException("El código no es válido: supera el largo permitido.");
        }
        DetallePackage detalle = repo.findByCodigoQRWithRelations(codigoQR)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Código QR no válido"));
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
