package com.example.ProyectoDesarrollo.Controllers.API;

import com.example.ProyectoDesarrollo.DTO.CambiarEstadoUsuarioRequest;
import com.example.ProyectoDesarrollo.DTO.PaginaResponse;
import com.example.ProyectoDesarrollo.DTO.ResetPasswordRequest;
import com.example.ProyectoDesarrollo.DTO.UsuarioActualizarRequest;
import com.example.ProyectoDesarrollo.DTO.UsuarioCrearRequest;
import com.example.ProyectoDesarrollo.DTO.UsuarioResponse;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/** Administración de usuarios (solo ADMINISTRADOR, validado en el servicio). */
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

    /** Listado paginado con los mismos filtros que el listado simple. */
    @GetMapping("/pagina")
    public PaginaResponse<UsuarioResponse> pagina(
            @RequestParam(name = "busqueda", required = false) String busqueda,
            @RequestParam(name = "rol", required = false) Usuario.Rol rol,
            @RequestParam(name = "estado", required = false) Usuario.Estado estado,
            @RequestParam(name = "pagina", defaultValue = "0") int pagina,
            @RequestParam(name = "tamanio", defaultValue = "10") int tamanio) {
        return usuarioService.buscarPaginado(busqueda, rol, estado, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.from(usuarioService.obtenerPorId(id));
    }

    /** Alta de un usuario por el administrador (cualquier rol). */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.from(usuarioService.crearPorAdmin(request)));
    }

    /** Edita nombre, apellido y rol. */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody UsuarioActualizarRequest request,
                                      Principal principal) {
        return UsuarioResponse.from(usuarioService.actualizar(id, request, solicitante(principal)));
    }

    /** El administrador fija una contraseña nueva para el usuario. */
    @PostMapping("/{id}/password")
    public ResponseEntity<Void> resetearPassword(@PathVariable Long id,
                                                 @Valid @RequestBody ResetPasswordRequest request) {
        usuarioService.resetearPassword(id, request.getPasswordNueva());
        return ResponseEntity.noContent().build();
    }

    /** Baja lógica: el usuario queda en estado ELIMINADO. */
    @DeleteMapping("/{id}")
    public UsuarioResponse eliminar(@PathVariable Long id, Principal principal) {
        return UsuarioResponse.from(usuarioService.eliminar(id, solicitante(principal)));
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

    private static String solicitante(Principal principal) {
        if (principal == null) {
            throw new AuthenticationCredentialsNotFoundException("Debes iniciar sesión");
        }
        return principal.getName();
    }
}
