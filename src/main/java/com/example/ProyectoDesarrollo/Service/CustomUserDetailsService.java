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

        if (dni == null || dni.isBlank()) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        String documento = dni.trim();


        if (!documento.matches("\\d{8}")) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        Usuario usuario = usuarioRepository.findByDNI(documento)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));


        if (usuario.getEstado() == null) {
            throw new DisabledException("La cuenta no tiene un estado válido");
        }
        if (usuario.getEstado() == Usuario.Estado.INNACTIVO) {
            throw new DisabledException("La cuenta está bloqueada");
        }
        if (usuario.getEstado() == Usuario.Estado.ELIMINADO) {
            throw new DisabledException("La cuenta fue eliminada");
        }


        if (usuario.getRol() == null) {
            throw new DisabledException("La cuenta no tiene un rol asignado");
        }

        return User.withUsername(usuario.getDNI())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }
}
