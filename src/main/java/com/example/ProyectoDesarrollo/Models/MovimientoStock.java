package com.example.ProyectoDesarrollo.Models;

import jakarta.persistence.*;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_stock")
public class MovimientoStock {
    public enum Tipo{
        INICIAL, ENTRADA, SALIDA, AJUSTE, PAQUETE, DEVOLUCION
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento",nullable = false)
    private Long idMovimiento;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "id_producto",nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo",nullable = false,length = 20)
    private Tipo tipo;

    @Column(name = "cantidad",nullable = false)
    private int cantidad;

    @Column(name = "stock_anterior",nullable = false)
    private int stockAnterior;

    @Column(name = "stock_nuevo",nullable = false)
    private int stockNuevo;

    @Column(name = "referencia",length = 100)
    private String referencia;

    @Column(name = "usuario_dni",length = 20)
    private String usuarioDni;

    @Column(name="fecha",nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void antesDeGuardar(){
        if(fecha==null){
            fecha=LocalDateTime.now();
        }
    }

    public MovimientoStock(){}

    public Long getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Long idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getStockAnterior() {
        return stockAnterior;
    }

    public void setStockAnterior(int stockAnterior) {
        this.stockAnterior = stockAnterior;
    }

    public int getStockNuevo() {
        return stockNuevo;
    }

    public void setStockNuevo(int stockNuevo) {
        this.stockNuevo = stockNuevo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getUsuarioDni() {
        return usuarioDni;
    }

    public void setUsuarioDni(String usuarioDni) {
        this.usuarioDni = usuarioDni;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
