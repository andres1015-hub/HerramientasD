package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Models.Producto;
import com.example.ProyectoDesarrollo.Services.InventarioService;
import com.example.ProyectoDesarrollo.Services.PaqueteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
public class EmpaquetadorController {

    private final InventarioService inventarioService;
    private final PaqueteService paqueteService;

    public EmpaquetadorController(InventarioService inventarioService,
                                  PaqueteService paqueteService) {
        this.inventarioService = inventarioService;
        this.paqueteService = paqueteService;
    }

    @GetMapping("/paquete")
    public String empaquetador(@RequestParam(name = "creado", required = false) Long creado,
                               Model model) {
        List<Producto> productos = inventarioService.listarTodos().stream()
                .sorted(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("productos", productos);
        model.addAttribute("paquetes", paqueteService.listarRecientes());
        if (creado != null) {
            model.addAttribute("paqueteCreado", paqueteService.obtenerPorId(creado));
        }
        return "empaquetador";
    }
}
