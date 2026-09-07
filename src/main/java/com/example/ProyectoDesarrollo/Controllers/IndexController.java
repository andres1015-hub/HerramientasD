package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Services.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    private final InventoryService inventoryService;

    public IndexController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("dashboard", inventoryService.dashboard());
        return "index";
    }
}
