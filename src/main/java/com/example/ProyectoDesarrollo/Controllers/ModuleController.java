package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Services.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ModuleController {

    private final InventoryService inventoryService;

    public ModuleController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventario")
    public String inventory(Model model) {
        model.addAttribute("products", inventoryService.products());
        return "inventory";
    }

    @GetMapping("/pedidos")
    public String orders(Model model) {
        model.addAttribute("orders", List.of(
                new OrderSummary("PED-001", "Almacén central", LocalDate.now().minusDays(4), "Recibido"),
                new OrderSummary("PED-002", "Taller de mantenimiento", LocalDate.now().minusDays(2), "En revisión"),
                new OrderSummary("PED-003", "Área de despacho", LocalDate.now(), "Registrado")
        ));
        return "orders";
    }

    @GetMapping("/reportes")
    public String reports(Model model) {
        model.addAttribute("categories", inventoryService.categorySummary());
        return "reports";
    }

    @GetMapping("/usuarios")
    public String users(Model model) {
        model.addAttribute("moduleTitle", "Usuarios");
        model.addAttribute("activePage", "usuarios");
        model.addAttribute("moduleMessage", "La gestión de usuarios y la autenticación se implementarán en una siguiente etapa.");
        return "development";
    }

    public record OrderSummary(String code, String customer, LocalDate date, String status) {
    }
}
