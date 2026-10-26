package com.example.ProyectoDesarrollo.Security;

import com.example.ProyectoDesarrollo.Exceptions.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

/**
 * Configuración de seguridad de la aplicación.
 *
 * Nivel de protección:
 *  - Autenticación por formulario (DNI + contraseña cifrada con BCrypt).
 *  - Autorización por rutas y por roles (ADMINISTRADOR, USER, PROVEEDOR).
 *  - Protección de fijación de sesión y cierre que invalida la sesión.
 *  - CSRF activo en formularios web; exento solo en /api/** (clientes externos).
 *  - Errores 401/403 en JSON para la API y en la página de error para la web.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Identifica las peticiones dirigidas a la API REST. */
    private static final RequestMatcher API = request -> request.getRequestURI().startsWith("/api/");

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers("/", "/login", "/error", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // API REST: cualquier usuario autenticado
                .requestMatchers("/api/**").authenticated()
                // Empaquetado: administradores y operarios
                .requestMatchers("/paquete", "/paquete/**").hasAnyRole("ADMINISTRADOR", "USER")
                // Administración (zonas reservadas)
                .requestMatchers("/usuarios", "/usuarios/**", "/inventario", "/inventario/**").hasRole("ADMINISTRADOR")
                // Todo lo demás requiere iniciar sesión
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("dni")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                // Los clientes de la API (lectores QR) no envían formularios ni token CSRF.
                // Los formularios web conservan la protección CSRF por defecto.
                .ignoringRequestMatchers(API)
            )
            .sessionManagement(session -> session
                // Evita fijación de sesión: se genera un nuevo id al iniciar sesión.
                .sessionFixation(fixation -> fixation.changeSessionId())
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Sin autenticación: JSON 401 para /api/** y redirección al login para la web.
     */
    private AuthenticationEntryPoint entryPoint() {
        LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> puntos = new LinkedHashMap<>();
        puntos.put(API, (request, response, ex) -> escribirErrorJson(request, response,
                HttpStatus.UNAUTHORIZED, "No autenticado",
                "Debes iniciar sesión para acceder a este recurso"));

        return new DelegatingAuthenticationEntryPoint(puntos, new LoginUrlAuthenticationEntryPoint("/login"));
    }

    /**
     * Sin permisos: JSON 403 para /api/** y página de error (403) para la web.
     */
    private AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandler sinPermisosApi = (request, response, ex) -> escribirErrorJson(request, response,
                HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para realizar esta acción");
        AccessDeniedHandlerImpl sinPermisosWeb = new AccessDeniedHandlerImpl();

        return (request, response, ex) -> {
            if (API.matches(request)) {
                sinPermisosApi.handle(request, response, ex);
            } else {
                sinPermisosWeb.handle(request, response, ex);
            }
        };
    }

    /**
     * Escribe un ErrorResponse como JSON usando un formato fijo,
     * sin acoplarse a la versión de Jackson del proyecto.
     */
    private static void escribirErrorJson(HttpServletRequest request, HttpServletResponse response,
                                          HttpStatus estado, String error, String mensaje) throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse cuerpo = new ErrorResponse(estado.value(), error, mensaje, request.getRequestURI());
        response.getWriter().write(aJson(cuerpo));
    }

    private static String aJson(ErrorResponse e) {
        return "{\"timestamp\":\"" + escapar(e.getTimestamp() != null ? e.getTimestamp().toString() : "")
                + "\",\"estado\":" + e.getEstado()
                + ",\"error\":\"" + escapar(e.getError())
                + "\",\"mensaje\":\"" + escapar(e.getMensaje())
                + "\",\"ruta\":\"" + escapar(e.getRuta())
                + "\",\"erroresCampo\":[]}";
    }

    private static String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
