package com.example.ProyectoDesarrollo.DTO;

import java.util.ArrayList;
import java.util.List;

/** Totales del paquete en armado (productos, unidades y monto) con validación de stock. */
public class ResumenPaqueteResponse {

    private int productos;
    private int unidades;
    private double total;
    private boolean valido = true;
    private List<Linea> lineas = new ArrayList<>();
    private List<String> errores = new ArrayList<>();

    public int getProductos() { return productos; }
    public void setProductos(int productos) { this.productos = productos; }

    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) { this.unidades = unidades; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public List<Linea> getLineas() { return lineas; }
    public List<String> getErrores() { return errores; }

    public static class Linea {
        private final Long idProducto;
        private final String nombre;
        private final int cantidad;
        private final int stockDisponible;
        private final double precioUnitario;
        private final double subtotal;
        private final boolean stockSuficiente;

        public Linea(Long idProducto, String nombre, int cantidad, int stockDisponible,
                     double precioUnitario, double subtotal, boolean stockSuficiente) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.stockDisponible = stockDisponible;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
            this.stockSuficiente = stockSuficiente;
        }

        public Long getIdProducto() { return idProducto; }
        public String getNombre() { return nombre; }
        public int getCantidad() { return cantidad; }
        public int getStockDisponible() { return stockDisponible; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return subtotal; }
        public boolean isStockSuficiente() { return stockSuficiente; }
    }
}
