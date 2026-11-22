package com.example.ProyectoDesarrollo.DTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class ResumenGeneralResponse {
    private int productosActivos;
    private int productosInactivos;
    private int productosSinStock;
    private int productosStockBajo;
    private long unidadesEnStock;
    private double valorInventario;
    private Map<String, Long> usuariosPorEstado=new LinkedHashMap<>();
    private Map<String, Long> paquetesPorEstado=new LinkedHashMap<>();
    private double montoPaquetesNoCancelados;
    private List<TopProducto> topProductos=new ArrayList<>();

    public int getProductosActivos() {
        return productosActivos;
    }

    public void setProductosActivos(int productosActivos) {
        this.productosActivos = productosActivos;
    }

    public int getProductosInactivos() {
        return productosInactivos;
    }

    public void setProductosInactivos(int productosInactivos) {
        this.productosInactivos = productosInactivos;
    }

    public int getProductosSinStock() {
        return productosSinStock;
    }

    public void setProductosSinStock(int productosSinStock) {
        this.productosSinStock = productosSinStock;
    }

    public int getProductosStockBajo() {
        return productosStockBajo;
    }

    public void setProductosStockBajo(int productosStockBajo) {
        this.productosStockBajo = productosStockBajo;
    }

    public long getUnidadesEnStock() {
        return unidadesEnStock;
    }

    public void setUnidadesEnStock(long unidadesEnStock) {
        this.unidadesEnStock = unidadesEnStock;
    }

    public double getValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(double valorInventario) {
        this.valorInventario = valorInventario;
    }

    public Map<String, Long> getUsuariosPorEstado() {
        return usuariosPorEstado;
    }

    public void setUsuariosPorEstado(Map<String, Long> usuariosPorEstado) {
        this.usuariosPorEstado = usuariosPorEstado;
    }

    public Map<String, Long> getPaquetesPorEstado() {
        return paquetesPorEstado;
    }

    public void setPaquetesPorEstado(Map<String, Long> paquetesPorEstado) {
        this.paquetesPorEstado = paquetesPorEstado;
    }

    public double getMontoPaquetesNoCancelados() {
        return montoPaquetesNoCancelados;
    }

    public void setMontoPaquetesNoCancelados(double montoPaquetesNoCancelados) {
        this.montoPaquetesNoCancelados = montoPaquetesNoCancelados;
    }

    public List<TopProducto> getTopProductos() {
        return topProductos;
    }

    public void setTopProductos(List<TopProducto> topProductos) {
        this.topProductos = topProductos;
    }

    public  static class TopProducto{
        private final Long idProducto;
        private final String nombre;
        private final long unidades;

        public TopProducto(Long idProducto, String nombre, long unidades){
            this.idProducto=idProducto;
            this.nombre=nombre;
            this.unidades=unidades;
        }
        public Long getIdProducto(){return idProducto;}
        public String getNombre(){return nombre;}
        public long getUnidades(){return  unidades;}
    }
}
