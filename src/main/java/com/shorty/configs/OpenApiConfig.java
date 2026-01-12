package com.shorty.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shorty URL Shortener API")
                        .description(
                                "API documentation for the Shorty URL Shortener application. This service allows users to create shortened URLs, manage their mappings, and redirect to original URLs.")
                        .version("1.0.0")
                        .contact(new Contact().name("Shorty Team").email("support@shorty.com"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")));
    }
}
