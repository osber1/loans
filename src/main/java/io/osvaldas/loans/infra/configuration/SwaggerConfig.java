package io.osvaldas.loans.infra.configuration;

import static java.util.Collections.emptyList;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.OAS_30;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;

@Component
public class SwaggerConfig {

    @Bean
    public Docket swaggerConfiguration() {
        return new Docket(OAS_30)
            .select()
            .paths(PathSelectors.ant("/api/**"))
            .apis(basePackage("io.osvaldas.loans"))
            .build()
            .apiInfo(apiDetails());
    }

    private ApiInfo apiDetails() {
        return new ApiInfo(
            "Loans Application",
            "",
            "V1.0",
            "osvaldas.bernatavicius@gmail.com",
            new Contact("Osvaldas", "https://github.com/osber1", "osvaldas.bernatavicius@gmail.com"),
            "API License",
            "https://swagger.io/license/",
            emptyList());
    }
}
