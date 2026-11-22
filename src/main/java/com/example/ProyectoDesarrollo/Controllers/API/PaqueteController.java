package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.CambiarEstadoPaqueteRequest;
import com.example.ProyectoDesarrollo.DTO.CrearPaqueteRequest;
import com.example.ProyectoDesarrollo.DTO.PaginaResponse;
import com.example.ProyectoDesarrollo.DTO.PaqueteResponse;
import com.example.ProyectoDesarrollo.DTO.PaqueteResumenResponse;
import com.example.ProyectoDesarrollo.DTO.ResumenPaqueteRequest;
import com.example.ProyectoDesarrollo.DTO.ResumenPaqueteResponse;
import com.example.ProyectoDesarrollo.Models.Paquete;
import com.example.ProyectoDesarrollo.Services.PaqueteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService paqueteService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'USER')")
    public ResponseEntity<PaqueteResponse> crear(@Valid @RequestBody CrearPaqueteRequest request,
                                                 Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesi��n para empaquetar");
        }
        PaqueteResponse creado = paqueteService.crear(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    /** Historial paginado con filtros por estado, pedido (texto parcial) y rango de fechas de creación. */
    @GetMapping
    public PaginaResponse<PaqueteResumenResponse> listar(
            @RequestParam(name = "estado", required = false) Paquete.Estado estado,
            @RequestParam(name = "pedido", required = false) String pedido,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio) {
        return paqueteService.buscar(estado, pedido, desde, hasta, pagina, tamanio);
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

    /** Marca el paquete como Recibido o Cancelado (cancelar exige "motivo" y devuelve el stock). */
    @PatchMapping("/{id}/estado")
    public PaqueteResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoPaqueteRequest request) {
        return paqueteService.cambiarEstado(id, request.getEstado(), request.getMotivo());
    }
}
