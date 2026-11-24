package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Services.DetallePackageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmpaquetadorController {

    private final DetallePackageService detallePackageService;

    public EmpaquetadorController(DetallePackageService detallePackageService) {
        this.detallePackageService = detallePackageService;
    }

    @GetMapping("/paquete")
    public String empaquetador(@RequestParam(name = "creado", required = false) Long creado) {
        return creado == null ? "redirect:/operaciones/empaquetador" : "redirect:/operaciones/paquetes?detalle=" + creado;
    }

    @GetMapping("/paquete/qr/{id}")
    public String verQr(@PathVariable Long id, Model model) {
        model.addAttribute("detalle", detallePackageService.obtenerPorId(id));
        return "qr";
    }
}
