package com.example.ProyectoDesarrollo.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "detallePaquete")
public class DetallePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalles", nullable = false)
    private long idDetalles;

    @Column(name = "codigo_qr", unique = true, nullable = false)
    private String codigoQR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paquete", nullable = false)
    private Paquete paquete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    @Column(name = "nombre_producto", nullable = false, length = 50)
    private String nombreProducto;

    @Column(name = "tipo_producto", nullable = false, length = 30)
    private String tipoProducto;

    @Column(name = "precio_unitario", nullable = false)
    private double precioUnitario;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @PrePersist
    public void antesDeGuardar() {
        if (this.codigoQR == null) {
            this.codigoQR = UUID.randomUUID().toString();
        }
        if (this.fechaEmision == null) {
            this.fechaEmision = LocalDateTime.now();
        }
        if (this.producto != null) {
            if (this.nombreProducto == null) this.nombreProducto = producto.getNombre();
            if (this.tipoProducto == null)   this.tipoProducto = producto.getTipo();
            if (this.precioUnitario == 0)    this.precioUnitario = producto.getPrecio();
        }
    }

    public DetallePackage() {
    }

    public DetallePackage(long idDetalles, String codigoQR, Paquete paquete, Producto producto,
                          int cantidad, String nombreProducto, String tipoProducto,
                          double precioUnitario, LocalDateTime fechaEmision) {
        this.idDetalles = idDetalles;
        this.codigoQR = codigoQR;
        this.paquete = paquete;
        this.producto = producto;
        this.cantidad = cantidad;
        this.nombreProducto = nombreProducto;
        this.tipoProducto = tipoProducto;
        this.precioUnitario = precioUnitario;
        this.fechaEmision = fechaEmision;
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

    public Paquete getPaquete() {
        return paquete;
    }

    public void setPaquete(Paquete paquete) {
        this.paquete = paquete;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}