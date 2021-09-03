package com.finance.loans.infra.configuration;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Component
public class SwaggerConfig {

    @Bean
    public Docket swaggerConfiguration() {
        return new Docket(DocumentationType.OAS_30)
            .select()
            .paths(PathSelectors.ant("/api/**"))
            .apis(RequestHandlerSelectors.basePackage("com.finance.loans"))
            .build()
            .apiInfo(apiDetails());
    }

    private ApiInfo apiDetails() {
        return new ApiInfo(
            "Interest Application",
            "Task for „4finance“",
            "V1.0",
            "osvaldas.bernatavicius@gmail.com",
            new springfox.documentation.service.Contact("Osvaldas", "https://github.com/osber1", "osvaldas.bernatavicius@gmail.com"),
            "API License",
            "https://swagger.io/license/",
            Collections.emptyList());
    }
}
