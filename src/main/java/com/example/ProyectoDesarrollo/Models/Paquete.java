package com.example.ProyectoDesarrollo.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paquete")
public class Paquete {

    public enum Estado {
        Enviado, Recibido, Cancelado
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paquete", nullable = false)
    private long idPaquete;

    @Column(name = "pedido", nullable = false, length = 50)
    private String nPedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private Estado estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cambio_estado")
    private LocalDateTime fechaCambioEstado;

    @Column(name = "motivo_cancelacion",length = 255)
    private String motivoCancelacion;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "paquete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePackage> detalles = new ArrayList<>();

    @PrePersist
    public void antesDeGuardar(){
        if(fechaCreacion==null){
            fechaCreacion=LocalDateTime.now();
        }
    }

    public Paquete() {
    }

    public Paquete(long idPaquete, String nPedido, Estado estado, Usuario usuario, List<DetallePackage> detalles) {
        this.idPaquete = idPaquete;
        this.nPedido = nPedido;
        this.estado = estado;
        this.usuario = usuario;
        this.detalles = detalles;
    }

    public long getIdPaquete() { return idPaquete; }
    public void setIdPaquete(long idPaquete) { this.idPaquete = idPaquete; }

    public String getnPedido() { return nPedido; }
    public void setnPedido(String nPedido) { this.nPedido = nPedido; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<DetallePackage> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePackage> detalles) { this.detalles = detalles; }

    public LocalDateTime getFechaCreacion(){return fechaCreacion;}
    public void setFechaCreacion(LocalDateTime fechaCreacion){this.fechaCreacion=fechaCreacion;}

    public LocalDateTime getFechaCambioEstado(){return fechaCambioEstado;}
    public void setFechaCambioEstado(LocalDateTime fechaCambioEstado){fechaCambioEstado=this.fechaCambioEstado;}

    public String getMotivoCancelacion(){return motivoCancelacion;}
    public void setMotivoCancelacion(String motivoCancelacion){motivoCancelacion=this.motivoCancelacion;}
}