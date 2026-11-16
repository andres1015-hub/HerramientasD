package com.example.ProyectoDesarrollo.DTO;

import com.example.ProyectoDesarrollo.Models.Usuario;

/** Vista pública de un usuario: nunca expone la contraseña. */
public class UsuarioResponse {

    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String dni;
    private String rol;
    private String estado;

    public static UsuarioResponse from(Usuario u) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.idUsuario = u.getIdUsuarios();
        dto.nombre = u.getNombre();
        dto.apellido = u.getApelllido();
        dto.dni = u.getDNI();
        dto.rol = u.getRol() != null ? u.getRol().name() : null;
        dto.estado = u.getEstado() != null ? u.getEstado().name() : null;
        return dto;
    }

    public Long getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getRol() { return rol; }
    public String getEstado() { return estado; }
}
