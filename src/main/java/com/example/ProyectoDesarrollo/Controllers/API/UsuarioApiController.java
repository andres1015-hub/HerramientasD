package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.CambiarEstadoUsuarioRequest;
import com.example.ProyectoDesarrollo.DTO.UsuarioResponse;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioApiController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(name = "busqueda", required = false) String busqueda,
                                        @RequestParam(name = "rol", required = false) Usuario.Rol rol,
                                        @RequestParam(name = "estado", required = false) Usuario.Estado estado) {
        return usuarioService.buscar(busqueda, rol, estado).stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoUsuarioRequest request,
                                         Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesión");
        }
        return UsuarioResponse.from(
                usuarioService.cambiarEstado(id, request.getEstado(), principal.getName()));
    }
}
