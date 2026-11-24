package com.example.ProyectoDesarrollo;

import com.example.ProyectoDesarrollo.Repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProyectoDesarrolloApplicationTests {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void noCreaCuentasAdministradorConCredencialesPublicas() {
        org.junit.jupiter.api.Assertions.assertTrue(usuarioRepository.findByDNI("12345678").isEmpty());
        org.junit.jupiter.api.Assertions.assertTrue(usuarioRepository.findByDNI("01234567").isEmpty());
    }

    @Test
    void apiRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("No autenticado")));
    }
}
