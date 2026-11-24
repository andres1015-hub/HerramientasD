package com.example.ProyectoDesarrollo.Security;

import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("demo")
public class DataInitializer {

    @Bean
    CommandLineRunner crearUsuarioDePrueba(UsuarioRepository repo,
                                           PasswordEncoder encoder,
                                           @Value("${DEMO_ADMIN_PASSWORD:}") String demoAdminPassword) {
        return args -> {
            if (demoAdminPassword == null || demoAdminPassword.length() < 12
                    || demoAdminPassword.length() > 100) {
                throw new IllegalStateException(
                        "DEMO_ADMIN_PASSWORD debe tener entre 12 y 100 caracteres para activar el perfil demo");
            }
            if (repo.findByDNI("12345678").isEmpty()) {
                Usuario u = new Usuario();
                u.setNombre("ADMINISTRADOR");
                u.setApelllido("Prueba");
                u.setDNI("12345678");
                u.setPassword(encoder.encode(demoAdminPassword));
                u.setEstado(Usuario.Estado.ACTIVO);
                u.setRol(Usuario.Rol.ADMINISTRADOR);
                repo.save(u);
            } else {
                Usuario u = repo.findByDNI("12345678").orElseThrow();
                u.setPassword(encoder.encode(demoAdminPassword));
                repo.save(u);
            }
        };
    }
}
