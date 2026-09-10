package com.example.ProyectoDesarrollo.Models;
import jakarta.persistence.*;
@Entity
@Table(name = "detallePaquete")
public class DetallePackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalles",nullable = false)
    private long idDetalles;
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

    public DetallePackage(long idDetalles, Paquete paquete, Producto producto, int cantidad) {
        this.idDetalles = idDetalles;
        this.paquete = paquete;
        this.producto = producto;
        this.cantidad = cantidad;
    }
}
