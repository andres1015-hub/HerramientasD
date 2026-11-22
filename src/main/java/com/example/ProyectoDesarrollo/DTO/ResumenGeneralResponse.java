package com.example.ProyectoDesarrollo.DTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Indicadores para el panel de administración. */
public class ResumenGeneralResponse {

    private int productosActivos;
    private int productosInactivos;
    private int productosSinStock;
    private int productosStockBajo;
    private long unidadesEnStock;
    private double valorInventario;
    private Map<String, Long> usuariosPorEstado = new LinkedHashMap<>();
    private Map<String, Long> paquetesPorEstado = new LinkedHashMap<>();
    private double montoPaquetesNoCancelados;
    private List<TopProducto> topProductos = new ArrayList<>();

    public int getProductosActivos() { return productosActivos; }
    public void setProductosActivos(int v) { this.productosActivos = v; }

    public int getProductosInactivos() { return productosInactivos; }
    public void setProductosInactivos(int v) { this.productosInactivos = v; }

    public int getProductosSinStock() { return productosSinStock; }
    public void setProductosSinStock(int v) { this.productosSinStock = v; }

    public int getProductosStockBajo() { return productosStockBajo; }
    public void setProductosStockBajo(int v) { this.productosStockBajo = v; }

    public long getUnidadesEnStock() { return unidadesEnStock; }
    public void setUnidadesEnStock(long v) { this.unidadesEnStock = v; }

    public double getValorInventario() { return valorInventario; }
    public void setValorInventario(double v) { this.valorInventario = v; }

    public Map<String, Long> getUsuariosPorEstado() { return usuariosPorEstado; }
    public Map<String, Long> getPaquetesPorEstado() { return paquetesPorEstado; }

    public double getMontoPaquetesNoCancelados() { return montoPaquetesNoCancelados; }
    public void setMontoPaquetesNoCancelados(double v) { this.montoPaquetesNoCancelados = v; }

    public List<TopProducto> getTopProductos() { return topProductos; }

    public static class TopProducto {
        private final Long idProducto;
        private final String nombre;
        private final long unidades;

        public TopProducto(Long idProducto, String nombre, long unidades) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.unidades = unidades;
        }

        public Long getIdProducto() { return idProducto; }
        public String getNombre() { return nombre; }
        public long getUnidades() { return unidades; }
    }
}
