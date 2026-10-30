package com.example.ProyectoDesarrollo.Controllers;

@Controller
public class RegistroController{
    @GetMapping("/registro")
    public String registro() {
        return "registro.html"
    }
}