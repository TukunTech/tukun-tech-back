package com.upc.tukuntech.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(CorsProps.class)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProps props) {
        CorsConfiguration cfg = new CorsConfiguration();

        List<String> origins = props.allowedOrigins();
        boolean creds = props.allowCredentials();

        if (creds && origins != null && origins.stream().anyMatch("*"::equals)) {
            cfg.setAllowedOriginPatterns(List.of("*"));
        } else if (origins != null && !origins.isEmpty()) {
            cfg.setAllowedOrigins(origins);
        } else {
            cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        }

        List<String> methods = props.allowedMethods();
        cfg.setAllowedMethods(
                (methods == null || methods.isEmpty())
                        ? List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        : methods
        );

        List<String> headers = props.allowedHeaders();
        cfg.setAllowedHeaders(
                (headers == null || headers.isEmpty())
                        ? List.of("*")
                        : headers
        );

        cfg.setAllowCredentials(creds);

        if (props.exposedHeaders() != null && !props.exposedHeaders().isEmpty()) {
            cfg.setExposedHeaders(props.exposedHeaders());
        } else {
            cfg.setExposedHeaders(List.of("Authorization", "Location"));
        }

        Long maxAge = props.maxAge();
        cfg.setMaxAge(Objects.requireNonNullElse(maxAge, 3600L)); // 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}