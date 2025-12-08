package org.th.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("MyTogether API")
                                                .version("1.0.0")
                                                .description("Spring Boot REST API with JWT Authentication and Google Maps integration")
                                                .contact(new Contact()
                                                                .name("MyTogether Team")
                                                                .email("contact@mytogether.com")
                                                                .url("https://github.com/banbannhk/MyTogether"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                                // Add Servers (Order matters! The first one is default)
                                .servers(List.of(
                                                new Server().url("https://mytogether-production.up.railway.app")
                                                                .description("Production Server (Railway)"),
                                                new Server().url("http://localhost:8080")
                                                                .description("Local Development Server")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .in(SecurityScheme.In.HEADER)
                                                                .name("Authorization")
                                                                .description("Enter JWT token in format: Bearer <token>")))
                                .addSecurityItem(new SecurityRequirement()
                                                .addList("bearer-jwt"));
        }
}