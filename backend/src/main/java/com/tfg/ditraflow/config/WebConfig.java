package com.tfg.ditraflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // MODIFICADO: Usamos allowedOriginPatterns para saltarnos la restricción estricta del navegador
                .allowedOriginPatterns(
                    "http://localhost:5173", 
                    "http://192.168.0.101:5173", 
                    "http://*.shadowzones.org",    // Soporta cualquier subdominio
                    "https://*.shadowzones.org",   // Soporta HTTPS con subdominios
                    "http://shadowzones.org",
                    "https://shadowzones.org"
                ) 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Set-Cookie")
                .allowCredentials(true)
                .maxAge(3600); 
    }
}