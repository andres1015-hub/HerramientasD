package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Services.InventoryService;
import com.example.ProyectoDesarrollo.Services.ProjectDataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModuleController {

    private final InventoryService inventoryService;
    private final ProjectDataService projectDataService;

    public ModuleController(InventoryService inventoryService, ProjectDataService projectDataService) {
        this.inventoryService = inventoryService;
        this.projectDataService = projectDataService;
    }

    @GetMapping("/inventario")
    public String inventory(Model model) {
        model.addAttribute("products", inventoryService.products());
        return "inventory";
    }

    @GetMapping("/pedidos")
    public String orders(Model model) {
        model.addAttribute("orders", projectDataService.orders());
        return "orders";
    }

    @GetMapping("/reportes")
    public String reports(Model model) {
        model.addAttribute("categories", inventoryService.categorySummary());
        return "reports";
    }

    @GetMapping("/usuarios")
    public String users(Model model) {
        model.addAttribute("users", projectDataService.users());
        return "users";
    }
}
