package com.upc.tukuntech.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.tukuntech.backend.shared.api.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final ObjectMapper mapper = new ObjectMapper();

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(this::write401)
                        .accessDeniedHandler(this::write403)
                )

                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/api-docs/**", "/v3/api-docs/**",
                                "/actuator/health",
                                "/auth/login"
                        ).permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }

    private void write401(HttpServletRequest req, HttpServletResponse res, Exception e) throws IOException {
        var body = ApiError.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                (e != null && e.getMessage() != null) ? "Credenciales inválidas" : "No autenticado",
                req.getRequestURI()
        );
        writeJson(res, HttpStatus.UNAUTHORIZED.value(), body);
    }

    private void write403(HttpServletRequest req, HttpServletResponse res,
                          org.springframework.security.access.AccessDeniedException e) throws IOException {
        var body = ApiError.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "No tienes permisos para esta operación",
                req.getRequestURI()
        );
        writeJson(res, HttpStatus.FORBIDDEN.value(), body);
    }

    private void writeJson(HttpServletResponse res, int status, Object body) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(res.getWriter(), body);
    }
}