package com.example.ProyectoDesarrollo.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bloquea el acceso directo a las APIs desde la barra de direcciones del navegador.
 * Una navegacion directa envia Sec-Fetch-Mode: navigate, mientras que las peticiones
 * realizadas por la aplicacion (fetch/img) usan otros modos y no se ven afectadas.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiDirectAccessFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) {
            String modo = request.getHeader("Sec-Fetch-Mode");
            if ("navigate".equalsIgnoreCase(modo)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\":\"Acceso directo a la API no permitido\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
