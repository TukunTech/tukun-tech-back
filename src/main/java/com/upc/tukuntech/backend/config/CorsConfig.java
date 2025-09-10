package com.upc.tukuntech.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProps.class)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProps props) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(props.allowedOrigins());
        cfg.setAllowedMethods(props.allowedMethods());
        cfg.setAllowedHeaders(props.allowedHeaders());
        cfg.setAllowCredentials(props.allowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
