package com.example.ProyectoDesarrollo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmpaquetadorController {
    @GetMapping("/paquete")
    public String empaquetador(Model model){
        return "empaquetador";
    }
}
