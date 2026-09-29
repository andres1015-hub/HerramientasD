package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    List<Usuario> findByNombre(String nombre);
    List<Usuario> findByDNI(String DNI);
    List<Usuario> findByRol(Usuario.Rol rol);
}
