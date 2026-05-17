package com.example.tester.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Test Tizimi API")
                        .description("Menejment 2024-2025 — imtihonga tayyorgarlik platformasi")
                        .version("1.0.0"));
    }
}
