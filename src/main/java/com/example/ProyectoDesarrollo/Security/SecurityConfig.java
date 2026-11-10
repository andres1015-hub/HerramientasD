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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcherEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    private static final RequestMatcher API = request -> request.getRequestURI().startsWith("/api/");

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                        // Público: inicio, login, auto-registro y recursos estáticos
                        .requestMatchers("/", "/login", "/registro", "/error", "/css/**", "/js/**",
                                "/images/**", "/favicon.ico").permitAll()

                        // Operación (ADMINISTRADOR y USER): empaquetado, escáner y API de QR
                        .requestMatchers("/paquete", "/paquete/**", "/escaner", "/escaner/**", "/api/**")
                                .hasAnyRole("ADMINISTRADOR", "USER")

                        // Administración: solo ADMINISTRADOR
                        .requestMatchers("/usuarios", "/usuarios/**", "/inventario", "/inventario/**")
                                .hasRole("ADMINISTRADOR")

                        // Cuenta propia: cualquier rol autenticado (incluye PROVEEDOR)
                        .requestMatchers("/miperfil", "/miperfil/**").authenticated()

                        // Resto: cualquier sesión válida (PROVEEDOR: inicio y catálogo)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("dni")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/")
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
                // CSRF activo también para /api/**: la sesión viaja en cookie y
                // las vistas envían el token en la cabecera configurada.
                .csrf(csrf -> {
                    // Se mantiene la protección CSRF por defecto, también en /api/**.
                })
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; base-uri 'self'; form-action 'self'; "
                                        + "frame-ancestors 'none'; img-src 'self' data:; "
                                        + "style-src 'self'; script-src 'self'; connect-src 'self'; "
                                        + "media-src 'self' blob:; object-src 'none'")))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.changeSessionId())
                        // Una sola sesión activa por usuario: al iniciar sesión en otro
                        // navegador, la sesión anterior queda invalidada.
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expirada")
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

    /** Registro compartido de sesiones para poder limitar sesiones concurrentes. */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /** Notifica a Spring Security cuándo se crea o destruye una sesión HTTP. */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


    private AuthenticationEntryPoint entryPoint() {
        AuthenticationEntryPoint apiEntryPoint = (request, response, ex) -> escribirErrorJson(request, response,
                HttpStatus.UNAUTHORIZED, "No autenticado",
                "Debes iniciar sesión para acceder a este recurso");

        List<RequestMatcherEntry<AuthenticationEntryPoint>> puntos =
                List.of(new RequestMatcherEntry<>(API, apiEntryPoint));

        return new DelegatingAuthenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"), puntos);
    }


    private AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandler sinPermisosApi = (request, response, ex) -> escribirErrorJson(request, response,
                HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para realizar esta acción");
        AccessDeniedHandler sinPermisosWeb = (request, response, ex) ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN);

        return (request, response, ex) -> {
            if (API.matches(request)) {
                sinPermisosApi.handle(request, response, ex);
            } else {
                sinPermisosWeb.handle(request, response, ex);
            }
        };
    }


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