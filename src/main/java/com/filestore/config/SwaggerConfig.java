package com.filestore.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Distributed File Storage API")
                        .description(
                                "REST API for file storge system"
                        ).version("v1.0")
                        .contact(new Contact().name("Shyam Singh").email("Shyam@email.com")))
                .addSecurityItem(
                        new SecurityRequirement().addList("Bearer Authentication")
                ).components(new Components()
                        .addSecuritySchemes(
                                "Bearer Authentication",new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));

    }

}
