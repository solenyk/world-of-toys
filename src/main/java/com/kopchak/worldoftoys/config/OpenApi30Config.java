package com.kopchak.worldoftoys.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "World of toys project",
                version = "1.0.0",
                description = "This is a backend API for a toy store web application built using Spring Boot 3, " +
                        "Spring Security 6 and Java 17. This API is responsible for handling all of the requests made " +
                        "to the server and returning the appropriate data to the front-end.",
                contact = @Contact(
                        name = "Iryna Kopchak",
                        email = "iryna.kopchak39@gmail.com",
                        url = "https://www.linkedin.com/in/iryna-kopchak/"
                ),
                license = @License(name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        externalDocs = @ExternalDocumentation(description = "Instructions for how to run and use this project",
                url = "https://github.com/shmatochok-maselka/world-of-toys/blob/develop/README.md"
        )
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApi30Config {

}
