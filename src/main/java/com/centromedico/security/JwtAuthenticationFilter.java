package com.centromedico.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Revisa el encabezado Authorization de cada petición. Si trae un JWT válido,
 * deja al usuario autenticado para el resto del procesamiento.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String encabezado = request.getHeader("Authorization");

        if (encabezado != null && encabezado.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            Claims datos = jwtService.validarYObtenerDatos(encabezado.substring(7));

            if (datos != null) {
                String username = datos.getSubject();
                String rol = datos.get("rol", String.class);

                var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                var autenticacion = new UsernamePasswordAuthenticationToken(username, null, autoridades);

                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        }

        filterChain.doFilter(request, response);
    }
}
