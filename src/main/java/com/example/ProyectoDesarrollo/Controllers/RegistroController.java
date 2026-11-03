package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.DTO.RegistroRequest;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Services.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistroController {

    private static final Logger log = LoggerFactory.getLogger(RegistroController.class);

    private final UsuarioService usuarioService;

    public RegistroController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String registro() {
        return "redirect:/?vista=registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute RegistroRequest datos,
                            BindingResult resultado,
                            RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            String mensaje = resultado.getFieldErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse("Revisa los datos ingresados");
            flash.addFlashAttribute("errorRegistro", mensaje);
            return "redirect:/?vista=registro";
        }

        try {
            usuarioService.registrar(datos);
        } catch (DatosInvalidosException | ReglaNegocioException ex) {
            log.warn("Registro rechazado para el DNI {}: {}", datos.getDNI(), ex.getMessage());
            flash.addFlashAttribute("errorRegistro", ex.getMessage());
            return "redirect:/?vista=registro";
        }

        flash.addFlashAttribute("exitoRegistro", "Cuenta creada correctamente. Ya puedes iniciar sesión.");
        return "redirect:/login?registrado";
    }
}
