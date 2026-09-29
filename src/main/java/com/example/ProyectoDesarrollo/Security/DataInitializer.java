package com.example.ProyectoDesarrollo.Security;

import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Crea un usuario de prueba (DNI 12345678 / clave admin123). Bórralo cuando ya tengas registro. */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner crearUsuarioDePrueba(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByDNI(12345678).isEmpty()) {
                Usuario u = new Usuario();
                u.setNombre("Admin");
                u.setApelllido("Prueba");
                u.setDNI(12345678);
                u.setPassword(encoder.encode("admin123"));
                repo.save(u);
            }
        };
    }
}
