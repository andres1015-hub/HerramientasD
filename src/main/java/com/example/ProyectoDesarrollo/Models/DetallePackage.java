package com.example.ProyectoDesarrollo.Models;
import jakarta.persistence.*;
@Entity
@Table(name = "detallePaquete")
public class DetallePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalles",nullable = false)
    private long idDetalles;
    @Column(name="codigo_qr",unique = true,nullable = false)
    private String codigoQR;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paquete",nullable = false)
    private Paquete paquete;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto",nullable = false)
    private Producto producto;
    @Column(name = "cantidad",nullable = false)
    private int cantidad;

    public DetallePackage() {
    }

    public DetallePackage(long idDetalles, String codigoQR, Paquete paquete, Producto producto, int cantidad) {
        this.idDetalles = idDetalles;
        this.codigoQR=codigoQR;
        this.paquete = paquete;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Paquete getPaquete() {
        return paquete;
    }

    public void setPaquete(Paquete paquete) {
        this.paquete = paquete;
    }

    public long getIdDetalles() {
        return idDetalles;
    }

    public void setIdDetalles(long idDetalles) {
        this.idDetalles = idDetalles;
    }

    public String getCodigoQR() {
        return codigoQR;
    }

    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }
}
