package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.RecursoNoEncontradoException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Services.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);
    private static final String VISTA_CUENTA = "redirect:/?vista=cuenta";

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/miperfil")
    public String cuenta() {
        return VISTA_CUENTA;
    }

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios";
    }

    @PostMapping("/miperfil/password")
    public String cambiarPassword(Principal principal,
                                  @RequestParam String passwordActual,
                                  @RequestParam String passwordNueva,
                                  @RequestParam String passwordConfirmacion,
                                  RedirectAttributes flash) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            usuarioService.cambiarPassword(principal.getName(), passwordActual,
                    passwordNueva, passwordConfirmacion);
            flash.addFlashAttribute("exitoCuenta", "Contraseña actualizada correctamente.");
        } catch (DatosInvalidosException | ReglaNegocioException | RecursoNoEncontradoException ex) {
            log.warn("Cambio de contraseña rechazado para {}: {}", principal.getName(), ex.getMessage());
            flash.addFlashAttribute("errorCuenta", ex.getMessage());
        }

        return VISTA_CUENTA;
    }
}
