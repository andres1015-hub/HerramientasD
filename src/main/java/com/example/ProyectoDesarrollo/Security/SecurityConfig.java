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

                        .requestMatchers("/", "/login", "/error", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/paquete", "/paquete/**").hasAnyRole("ADMINISTRADOR", "USER")

                        .requestMatchers("/usuarios", "/usuarios/**", "/inventario", "/inventario/**").hasRole("ADMINISTRADOR")

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
                        .ignoringRequestMatchers(API)
                )
                .sessionManagement(session -> session
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
        AccessDeniedHandlerImpl sinPermisosWeb = new AccessDeniedHandlerImpl();

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