package com.anup.restaurant_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * ============================================
 *   CORS Configuration
 * ============================================
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // Allow ALL origins (any localhost port, any frontend)
        // In production you would restrict this to your domain
        config.addAllowedOriginPattern("*");

        // Allow all HTTP methods
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("OPTIONS");

        // Allow all headers including Authorization (for JWT)
        config.addAllowedHeader("*");

        // Allow Authorization header to be sent
        // This is needed for JWT token to pass through
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply to ALL routes
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}