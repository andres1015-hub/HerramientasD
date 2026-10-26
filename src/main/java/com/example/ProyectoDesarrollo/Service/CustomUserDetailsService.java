package com.example.ProyectoDesarrollo.Service;

import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        // Entrada inválida: misma respuesta que un usuario inexistente
        // (evita revelar qué DNI está registrado).
        if (dni == null || dni.isBlank()) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        String documento = dni.trim();

        // El DNI debe ser exactamente 8 dígitos numéricos (sin letras ni símbolos).
        if (!documento.matches("\\d{8}")) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        Usuario usuario = usuarioRepository.findByDNI(documento)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        // Solo las cuentas ACTIVAS pueden iniciar sesión.
        if (usuario.getEstado() == null) {
            throw new DisabledException("La cuenta no tiene un estado válido");
        }
        if (usuario.getEstado() == Usuario.Estado.INNACTIVO) {
            throw new DisabledException("La cuenta está bloqueada");
        }
        if (usuario.getEstado() == Usuario.Estado.ELIMINADO) {
            throw new DisabledException("La cuenta fue eliminada");
        }

        // El rol se toma de la base de datos (ADMINISTRADOR, USER o PROVEEDOR).
        if (usuario.getRol() == null) {
            throw new DisabledException("La cuenta no tiene un rol asignado");
        }

        return User.withUsername(usuario.getDNI())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}
