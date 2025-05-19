package com.deliverar.pagos.adapters.rest.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Deliver.AR Pagos API",
                version = "v1",
                description = "Documentación de la API de Pagos"
        ),
        servers = @Server(url = "/")
)
public class SwaggerConfig {
    // Con esta configuración mínima, SpringDoc expone:
    // - OpenAPI JSON en /v3/api-docs
    // - Swagger UI en /swagger-ui.html
}
