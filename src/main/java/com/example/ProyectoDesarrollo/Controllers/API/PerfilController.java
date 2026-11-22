package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.PerfilActualizarRequest;
import com.example.ProyectoDesarrollo.DTO.UsuarioResponse;
import com.example.ProyectoDesarrollo.Services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Datos de la propia cuenta. Se publica bajo /miperfil (y no /api) porque esa ruta ya admite a
 * cualquier rol autenticado, incluido PROVEEDOR; /api/** solo admite ADMINISTRADOR y USER.
 */
@RestController
@RequestMapping("/miperfil/datos")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;

    @GetMapping
    public UsuarioResponse obtener(Principal principal) {
        return UsuarioResponse.from(usuarioService.obtenerPerfil(dni(principal)));
    }

    @PutMapping
    public UsuarioResponse actualizar(@Valid @RequestBody PerfilActualizarRequest request, Principal principal) {
        return UsuarioResponse.from(usuarioService.actualizarPerfil(dni(principal), request));
    }

    private static String dni(Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesión");
        }
        return principal.getName();
    }
}
