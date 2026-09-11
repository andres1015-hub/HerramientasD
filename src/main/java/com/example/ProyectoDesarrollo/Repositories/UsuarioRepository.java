package com.example.ProyectoDesarrollo.Repositories;

import com.example.ProyectoDesarrollo.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
