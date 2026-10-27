package com.example.ProyectoDesarrollo.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="usuarios")
public class Usuario {

    public enum Rol{
        ADMINISTRADOR, USER, PROVEEDOR
    }
    public enum Estado{
        ACTIVO, INNACTIVO, ELIMINADO
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuarios",nullable = false)
    private long idUsuarios;
    @Column(name = "nombre",length = 30, nullable = false)
    private String nombre;
    @Column(name = "apellido",length = 30, nullable = false)
    private String apelllido;
    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos numéricos")
    @Column(name = "DNI", length = 10, unique = true, nullable = false)
    private String DNI;
    @Column(name = "password",length = 100, nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(name = "rol",nullable = false,length = 30)
    private Usuario.Rol rol;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado",nullable=false,length =30)
    private Usuario.Estado estado;
    @OneToMany(mappedBy = "usuario")
    private List<Paquete>paquete=new ArrayList<>();

    public Usuario() {
    }

    public Usuario(long idUsuarios, String apelllido, String DNI, String nombre, String password, Rol rol,
                   Estado estado,  List<Paquete> usuario) {
        this.idUsuarios = idUsuarios;
        this.apelllido = apelllido;
        this.DNI = DNI;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
        this.estado= estado;
        this.paquete=usuario;
    }

    public long getIdUsuarios() {
        return idUsuarios;
    }

    public void setIdUsuarios(long idUsuarios) {
        this.idUsuarios = idUsuarios;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApelllido() {
        return apelllido;
    }

    public void setApelllido(String apelllido) {
        this.apelllido = apelllido;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol(){return  rol;}

    public void setRol(Rol rol){this.rol=rol;}

    public Estado getEstado() {return estado;}

    public void setEstado(Estado estado){this.estado=estado;}

    public List<Paquete> getDetalles() {
        return paquete;
    }

    public void setDetalles(List<Paquete> detalles) {
        this.paquete = detalles;
    }
}
