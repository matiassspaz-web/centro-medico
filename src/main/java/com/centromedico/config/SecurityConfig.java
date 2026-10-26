package com.centromedico.config;

import com.centromedico.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Las contraseñas nunca se guardan en texto plano: BCrypt genera un hash
     * distinto cada vez y no permite recuperar el original.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(peticiones -> peticiones
                        // Páginas y recursos del frontend
                        .requestMatchers("/", "/index.html", "/*.html",
                                "/css/**", "/img/**", "/js/**", "/favicon.ico").permitAll()
                        // Login
                        .requestMatchers("/api/auth/login").permitAll()
                        // Solo ADMIN puede dar de baja registros
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        // El resto de la API exige token válido
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint((peticion, respuesta, ex) -> responderJson(respuesta,
                                HttpStatus.UNAUTHORIZED, "No autorizado",
                                "Debe iniciar sesión para acceder a este recurso"))
                        .accessDeniedHandler((peticion, respuesta, ex) -> responderJson(respuesta,
                                HttpStatus.FORBIDDEN, "Acceso denegado",
                                "Su usuario no tiene permisos para realizar esta acción")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Los errores de seguridad también se devuelven como JSON, para que el
     * frontend los pueda mostrar igual que el resto de los errores de la API.
     */
    private void responderJson(HttpServletResponse respuesta, HttpStatus estado,
                               String error, String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write("""
                {"status":%d,"error":"%s","mensaje":"%s"}"""
                .formatted(estado.value(), error, mensaje));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // El frontend puede servirse desde otro dominio (Vercel), no solo desde
        // el propio backend, asi que esos origenes tambien se permiten.
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*",
                "https://*.vercel.app"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
