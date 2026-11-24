package com.example.ProyectoDesarrollo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class IndexController {
    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "inicio") String vista){
        String seccion = switch (vista) {
            case "inventario" -> "productos";
            case "cuenta" -> "cuenta";
            case "historial" -> "paquetes";
            case "movimientos", "auditoria" -> vista;
            case "administrador" -> "panel";
            default -> null;
        };
        if (seccion != null) return "redirect:/operaciones/" + seccion;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")))
            return "redirect:/operaciones/panel";
        return "index";
    }
}
