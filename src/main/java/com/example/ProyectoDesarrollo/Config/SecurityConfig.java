package com.example.ProyectoDesarrollo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // This academic iteration is open access until authentication and roles are implemented.
        // Keep the default CSRF protection; Thymeleaf adds the token to POST forms.
        http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives(
                        "default-src 'self'; script-src 'none'; style-src 'self'; "
                                + "img-src 'self' data:; object-src 'none'; frame-ancestors 'none'; "
                                + "base-uri 'self'; form-action 'self'")));
        return http.build();
    }

    @Bean
    UserDetailsService unavailableUserDetailsService() {
        // Avoid creating a default account/password while user authentication is unfinished.
        return username -> {
            throw new UsernameNotFoundException("La autenticación todavía no está habilitada");
        };
    }
}
