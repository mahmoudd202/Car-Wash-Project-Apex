package com.apex.carwash.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        Info info = new Info()
                .title("Car-wash booking API")
                .version("1.0")
                .description("Prices are SAR. Dates use UTC. Bookings require X-API-Key.");

        SecurityScheme apiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key");

        return new OpenAPI()
                .info(info)
                .components(new Components().addSecuritySchemes("apiKey", apiKeyScheme));
    }
}
