package com.example.demo.ProyectoDesarrollo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioController{
    @GetMapping("/miperfil")
    public String cuenta() {
        return "cuenta";
    }
}