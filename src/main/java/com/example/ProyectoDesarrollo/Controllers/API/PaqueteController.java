package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.CambiarEstadoPaqueteRequest;
import com.example.ProyectoDesarrollo.DTO.CrearPaqueteRequest;
import com.example.ProyectoDesarrollo.DTO.PaqueteResponse;
import com.example.ProyectoDesarrollo.DTO.ResumenPaqueteRequest;
import com.example.ProyectoDesarrollo.DTO.ResumenPaqueteResponse;
import com.example.ProyectoDesarrollo.Services.PaqueteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService paqueteService;

    @PostMapping
    public ResponseEntity<PaqueteResponse> crear(@Valid @RequestBody CrearPaqueteRequest request,
                                                 Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesión para empaquetar");
        }
        PaqueteResponse creado = paqueteService.crear(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @GetMapping("/recientes")
    public List<PaqueteResponse> recientes() {
        return paqueteService.listarRecientes();
    }

    @GetMapping("/{id}")
    public PaqueteResponse obtener(@PathVariable Long id) {
        return paqueteService.obtenerPorId(id);
    }

    /** Totales y validación de stock del paquete en armado (sin crearlo). */
    @PostMapping("/resumen")
    public ResumenPaqueteResponse resumen(@Valid @RequestBody ResumenPaqueteRequest request) {
        return paqueteService.calcularResumen(request);
    }

    /** Marca el paquete como Recibido o Cancelado (al cancelar se devuelve el stock). */
    @PatchMapping("/{id}/estado")
    public PaqueteResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoPaqueteRequest request) {
        return paqueteService.cambiarEstado(id, request.getEstado());
    }
}
