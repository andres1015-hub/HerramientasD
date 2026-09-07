package com.example.ProyectoDesarrollo.Models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductForm {

    @NotBlank(message = "Ingresa el código")
    @Size(max = 20, message = "El código admite hasta 20 caracteres")
    private String code;

    @NotBlank(message = "Ingresa el nombre")
    @Size(max = 80, message = "El nombre admite hasta 80 caracteres")
    private String name;

    @Size(max = 180, message = "La descripción admite hasta 180 caracteres")
    private String description;

    @NotBlank(message = "Ingresa la categoría")
    @Size(max = 50, message = "La categoría admite hasta 50 caracteres")
    private String category;

    @NotNull(message = "Ingresa el precio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal price = BigDecimal.ZERO;

    @NotNull(message = "Ingresa el stock")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;

    @NotNull(message = "Ingresa el stock mínimo")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer minimumStock = 0;

    public static ProductForm from(Product product) {
        ProductForm form = new ProductForm();
        form.setCode(product.code());
        form.setName(product.name());
        form.setDescription(product.description());
        form.setCategory(product.category());
        form.setPrice(product.price());
        form.setStock(product.stock());
        form.setMinimumStock(product.minimumStock());
        return form;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }
}
