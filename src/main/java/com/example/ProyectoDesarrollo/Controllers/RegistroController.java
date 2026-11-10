package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.DTO.RegistroRequest;
import com.example.ProyectoDesarrollo.Exceptions.DatosInvalidosException;
import com.example.ProyectoDesarrollo.Exceptions.ReglaNegocioException;
import com.example.ProyectoDesarrollo.Services.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    /**
     * Única puerta de entrada al registro: el enlace «Ver registro» del login.
     * La vista se monta sobre el mismo armazón de index.html para conservar
     * navegación, estilos y manejo de errores.
     */
    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("vistaInicial", "registro");
        return "index";
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
            return "redirect:/registro";
        }

        try {
            usuarioService.registrar(datos);
        } catch (DatosInvalidosException | ReglaNegocioException ex) {
            log.warn("Registro rechazado para el DNI {}: {}", datos.getDNI(), ex.getMessage());
            flash.addFlashAttribute("errorRegistro", ex.getMessage());
            return "redirect:/registro";
        }

        flash.addFlashAttribute("exitoRegistro", "Cuenta creada correctamente. Ya puedes iniciar sesión.");
        return "redirect:/login?registrado";
    }
}
