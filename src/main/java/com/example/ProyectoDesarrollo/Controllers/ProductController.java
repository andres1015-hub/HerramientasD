package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Models.Product;
import com.example.ProyectoDesarrollo.Models.ProductForm;
import com.example.ProyectoDesarrollo.Services.InventoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ProductController {

    private final InventoryService inventoryService;

    public ProductController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String list(@RequestParam(name = "q", defaultValue = "") String query, Model model) {
        model.addAttribute("products", inventoryService.search(query));
        model.addAttribute("query", query);
        return "products/list";
    }

    @GetMapping("/nuevo")
    public String createForm(Model model) {
        prepareForm(model, new ProductForm(), false, null);
        return "products/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, false, null);
            return "products/form";
        }
        try {
            Product product = inventoryService.create(form);
            redirectAttributes.addFlashAttribute("message", "Producto registrado correctamente.");
            return "redirect:/productos/" + product.id();
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("code", "duplicate", exception.getMessage());
            prepareForm(model, form, false, null);
            return "products/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("product", inventoryService.product(id));
        return "products/detail";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable long id, Model model) {
        Product product = inventoryService.product(id);
        prepareForm(model, ProductForm.from(product), true, id);
        return "products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable long id,
                         @Valid @ModelAttribute("productForm") ProductForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form, true, id);
            return "products/form";
        }
        try {
            inventoryService.update(id, form);
            redirectAttributes.addFlashAttribute("message", "Producto actualizado correctamente.");
            return "redirect:/productos/" + id;
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("code", "duplicate", exception.getMessage());
            prepareForm(model, form, true, id);
            return "products/form";
        }
    }

    @GetMapping("/{id}/eliminar")
    public String deleteConfirmation(@PathVariable long id, Model model) {
        model.addAttribute("product", inventoryService.product(id));
        return "products/delete";
    }

    @PostMapping("/{id}/eliminar")
    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        inventoryService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Producto eliminado del avance.");
        return "redirect:/productos";
    }

    private void prepareForm(Model model, ProductForm form, boolean editing, Long productId) {
        model.addAttribute("productForm", form);
        model.addAttribute("editing", editing);
        model.addAttribute("productId", productId);
    }
}
