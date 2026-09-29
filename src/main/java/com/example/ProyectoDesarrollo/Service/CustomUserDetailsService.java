package com.example.ProyectoDesarrollo.Service;

import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
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
        int dniNumero;
        try {
            dniNumero = Integer.parseInt(dni.trim());
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("DNI inválido");
        }

        Usuario usuario = usuarioRepository.findByDNI(dniNumero)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.withUsername(String.valueOf(usuario.getDNI()))
                .password(usuario.getPassword())
                .roles("USER")
                .build();
    }
}
