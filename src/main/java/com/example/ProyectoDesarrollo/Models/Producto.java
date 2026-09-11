package com.example.ProyectoDesarrollo.Models;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name="Producto", uniqueConstraints = @UniqueConstraint(name = "uk_producto_codigo", columnNames = "codigo"))
public class Producto {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_producto", nullable = false)
    private long idProducto;
    @Column(name="nombre",nullable = false,length = 50)
    private String nombre;
    @Column(name="stock",nullable = false,length = 30)
    private int stock;
    @Column(name="tipo",nullable = false,length = 30)
    private String tipo;
    @Column(name = "precio",nullable = false,length = 50)
    private double precio;

    // Nullable additions preserve products registered with the original schema.
    @Column(name = "codigo", length = 32)
    private String codigo;
    @Column(name = "descripcion", length = 180)
    private String descripcion;
    @Column(name = "stock_minimo")
    private Integer stockMinimo;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private Product.Status estado;
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "producto")
    private List<DetallePackage>detalles=new ArrayList<>();

    public Producto() {
    }

    public Producto(long idProducto, String nombre, int stock, String tipo, double precio, List<DetallePackage> detalles) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.stock = stock;
        this.tipo = tipo;
        this.precio = precio;
        this.detalles = detalles;
    }

    public long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(long idProducto) {
        this.idProducto = idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public List<DetallePackage> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePackage> detalles) {
        this.detalles = detalles;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Product.Status getEstado() {
        return estado;
    }

    public void setEstado(Product.Status estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
