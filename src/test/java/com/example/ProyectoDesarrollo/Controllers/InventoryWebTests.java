package com.example.ProyectoDesarrollo.Controllers;

import com.example.ProyectoDesarrollo.Services.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InventoryWebTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private InventoryService inventoryService;

    @Test
    void pagesRenderWithoutScriptsAndWithBrowserSecurityHeaders() throws Exception {
        for (String path : new String[]{"/", "/productos", "/productos/nuevo", "/inventario", "/pedidos",
                "/qr", "/reportes", "/usuarios", "/paquete"}) {
            mvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("<script"))))
                    .andExpect(header().string("Content-Security-Policy", containsString("script-src 'none'")))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                    .andExpect(header().string("X-Frame-Options", "DENY"));
        }
        mvc.perform(get("/css/styles.css")).andExpect(status().isOk());
    }

    @Test
    void renderedFormContainsUsableSessionCsrfToken() throws Exception {
        MvcResult formPage = mvc.perform(get("/productos/nuevo"))
                .andExpect(status().isOk())
                .andReturn();
        String token = csrfToken(formPage);
        MockHttpSession session = (MockHttpSession) formPage.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mvc.perform(validProduct(post("/productos"), "WEB-FORM", "Producto desde formulario")
                        .session(session).param("_csrf", token))
                .andExpect(status().is3xxRedirection());
        assertThat(inventoryService.search("WEB-FORM")).hasSize(1);
    }

    @Test
    void mutationsWithoutValidCsrfAreRejected() throws Exception {
        mvc.perform(validProduct(post("/productos"), "WEB-BLOCK", "Bloqueado"))
                .andExpect(status().isForbidden());
        mvc.perform(validProduct(post("/productos"), "WEB-BLOCK", "Bloqueado").with(csrf().useInvalidToken()))
                .andExpect(status().isForbidden());
        mvc.perform(validProduct(post("/productos/1"), "WEB-BLOCK", "Bloqueado"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/productos/1/eliminar"))
                .andExpect(status().isForbidden());
        assertThat(inventoryService.search("WEB-BLOCK")).isEmpty();
    }

    @Test
    void productCanBeCreatedEditedAndDeletedThroughServerRenderedForms() throws Exception {
        String detailUrl = createProduct("WEB-CRUD", "Producto inicial");
        mvc.perform(get(detailUrl)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Producto inicial")));

        MvcResult editForm = mvc.perform(get(detailUrl + "/editar"))
                .andExpect(status().isOk()).andReturn();
        assertThat(csrfToken(editForm)).isNotBlank();
        mvc.perform(validProduct(post(detailUrl), "WEB-CRUD", "Producto actualizado").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(detailUrl));
        mvc.perform(get(detailUrl)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Producto actualizado")));

        MvcResult deleteForm = mvc.perform(get(detailUrl + "/eliminar"))
                .andExpect(status().isOk()).andReturn();
        assertThat(csrfToken(deleteForm)).isNotBlank();
        mvc.perform(post(detailUrl + "/eliminar").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/productos"));
        mvc.perform(get(detailUrl)).andExpect(status().isNotFound());
    }

    @Test
    void productTextAndSearchQueryAreEscaped() throws Exception {
        String payload = "<script>alert(1)</script>";
        String detailUrl = createProduct("WEB-ESCAPE", payload);
        mvc.perform(get(detailUrl)).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")))
                .andExpect(content().string(not(containsString(payload))));
        mvc.perform(get("/productos").param("q", payload)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString(payload))));
    }

    @Test
    void invalidAndDuplicateProductsReturnFormErrors() throws Exception {
        mvc.perform(validProduct(post("/productos"), "WEB-INVALID", "").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("productForm", "name"));
        assertThat(inventoryService.search("WEB-INVALID")).isEmpty();

        createProduct("WEB-DUP", "Original");
        mvc.perform(validProduct(post("/productos"), "web-dup", "Duplicado").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("productForm", "code"));
        assertThat(inventoryService.search("WEB-DUP")).hasSize(1);
    }

    @Test
    void qrImageIsGeneratedAndUnknownProductsReturn404() throws Exception {
        String detailUrl = createProduct("WEB-QR", "Producto con QR");
        String id = detailUrl.substring(detailUrl.lastIndexOf('/') + 1);
        mvc.perform(get("/qr").param("productoId", id)).andExpect(status().isOk());
        mvc.perform(get("/qr/productos/" + id)).andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray())
                        .startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47));
        mvc.perform(get("/qr/productos/" + id).param("download", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment;")));
        mvc.perform(get("/qr").param("productoId", String.valueOf(Long.MAX_VALUE)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/qr/productos/" + Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    private String createProduct(String code, String name) throws Exception {
        String location = mvc.perform(validProduct(post("/productos"), code, name).with(csrf()))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        assertThat(location).matches("/productos/\\d+");
        return location;
    }

    private MockHttpServletRequestBuilder validProduct(MockHttpServletRequestBuilder request,
                                                       String code, String name) {
        return request.param("code", code).param("name", name).param("description", "Prueba MVC")
                .param("category", "Pruebas web").param("price", "12.50")
                .param("stock", "8").param("minimumStock", "2");
    }

    private String csrfToken(MvcResult result) throws Exception {
        Matcher token = Pattern.compile("<input\\b(?=[^>]*name=\"_csrf\")(?=[^>]*type=\"hidden\")[^>]*value=\"([^\"]+)\"")
                .matcher(result.getResponse().getContentAsString());
        assertThat(token.find()).as("POST form includes a hidden CSRF token").isTrue();
        return token.group(1);
    }
}
