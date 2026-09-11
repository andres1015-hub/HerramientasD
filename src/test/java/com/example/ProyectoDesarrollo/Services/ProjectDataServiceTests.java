package com.example.ProyectoDesarrollo.Services;

import com.example.ProyectoDesarrollo.Models.Paquete;
import com.example.ProyectoDesarrollo.Models.Usuario;
import com.example.ProyectoDesarrollo.Repositories.PaqueteRepository;
import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProjectDataServiceTests {
    @Autowired ProjectDataService service;
    @Autowired UsuarioRepository usuarios;
    @Autowired PaqueteRepository paquetes;

    @Test
    void readsExistingUserAndPackageRelationsWithoutExposingCredentials() {
        Usuario usuario = usuarios.saveAndFlush(
                new Usuario(0, "Equipo", 99990001, "Ana", "fixture-only", new ArrayList<>()));
        Paquete paquete = paquetes.saveAndFlush(
                new Paquete(0, "PED-EXISTENTE", Paquete.Estado.Recibido, usuario, new ArrayList<>()));

        assertThat(service.orders()).anySatisfy(order -> {
            assertThat(order.id()).isEqualTo(paquete.getIdPaquete());
            assertThat(order.code()).isEqualTo("PED-EXISTENTE");
            assertThat(order.customer()).isEqualTo("Ana Equipo");
            assertThat(order.status()).isEqualTo("Recibido");
        });
        assertThat(service.users()).anySatisfy(user -> {
            assertThat(user.id()).isEqualTo(usuario.getIdUsuarios());
            assertThat(user.name()).isEqualTo("Ana");
            assertThat(user.surname()).isEqualTo("Equipo");
        });
        assertThat(service.users().toString()).doesNotContain("fixture-only", "99990001");
    }
}
