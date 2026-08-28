package com.example.ProyectoDesarrollo.Models;

import jakarta.persistence.*;

@Entity
@Table(name="Usuarios")
public class Usuario {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuarios",nullable = false)
    private long idUsuarios;
    @Column(name = "nombre",length = 30, nullable = false)
    private String nombre;
    @Column(name = "apellido",length = 30, nullable = false)
    private String apelllido;
    @Column(name = "DNI", length = 30, unique = true, nullable = false)
    private int DNI;
    @Column(name = "password",length = 30, nullable = false)
    private String password;

    public Usuario() {
    }

    public Usuario(long idUsuarios, String apelllido, int DNI, String nombre, String password) {
        this.idUsuarios = idUsuarios;
        this.apelllido = apelllido;
        this.DNI = DNI;
        this.nombre = nombre;
        this.password = password;
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

    public int getDNI() {
        return DNI;
    }

    public void setDNI(int DNI) {
        this.DNI = DNI;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
