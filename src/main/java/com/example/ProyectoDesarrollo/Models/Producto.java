package com.example.ProyectoDesarrollo.Models;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Producto")
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
}
