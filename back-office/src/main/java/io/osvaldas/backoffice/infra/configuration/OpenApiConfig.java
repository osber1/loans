package io.osvaldas.backoffice.infra.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Component
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${info.application.name:}") String name,
                           @Value("${info.application.description:}") String description,
                           @Value("${info.application.version:}") String version) {
        return new OpenAPI()
            .info(new Info().title(name)
                .description(description)
                .version(version)
                .termsOfService("http://swagger.io/terms/")
                .license(new License().name("Apache 2.0").url("http://springdoc.org"))
                .contact(new Contact().name("Osvaldas Bernatavičius").url("https://github.com/osber1").email("osvaldas.bernatavicius@gmail.com")));
    }
}
