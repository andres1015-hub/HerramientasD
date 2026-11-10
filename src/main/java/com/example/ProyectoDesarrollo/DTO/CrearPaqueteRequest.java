package com.example.ProyectoDesarrollo.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos para generar un paquete y sus códigos QR.
 * Se valida en el borde (Bean Validation) y de nuevo en el servicio (reglas de negocio).
 */
public class CrearPaqueteRequest {

    @NotBlank(message = "El número de pedido es obligatorio")
    @Size(max = 50, message = "El número de pedido no puede superar los 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]{2,49}$",
            message = "Usa entre 3 y 50 caracteres: letras, números, punto, guion o guion bajo")
    private String numeroPedido;

    @NotEmpty(message = "Agrega al menos un producto al paquete")
    @Size(max = 50, message = "Un paquete no puede contener más de 50 productos distintos")
    private List<@Valid ItemPaquete> items = new ArrayList<>();

    public CrearPaqueteRequest() {
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public void setNumeroPedido(String numeroPedido) {
        this.numeroPedido = numeroPedido;
    }

    public List<ItemPaquete> getItems() {
        return items;
    }

    public void setItems(List<ItemPaquete> items) {
        this.items = items;
    }

    public static class ItemPaquete {

        @NotNull(message = "Cada producto debe tener un identificador")
        @Positive(message = "El identificador del producto no es válido")
        private Long idProducto;

        @NotNull(message = "Cada producto debe tener una cantidad")
        @Min(value = 1, message = "La cantidad mínima por producto es 1")
        @Max(value = 10000, message = "La cantidad máxima por producto es 10000")
        private Integer cantidad;

        public ItemPaquete() {
        }

        public ItemPaquete(Long idProducto, Integer cantidad) {
            this.idProducto = idProducto;
            this.cantidad = cantidad;
        }

        public Long getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(Long idProducto) {
            this.idProducto = idProducto;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }
}
