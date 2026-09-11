package com.example.ProyectoDesarrollo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmpaquetadorController {
    @GetMapping("/paquete")
    public String empaquetador(Model model) {
        model.addAttribute("moduleTitle", "Empaquetado");
        model.addAttribute("activePage", "productos");
        model.addAttribute("moduleMessage", "El flujo de empaquetado se incorporará en una siguiente etapa.");
        return "development";
    }
}
