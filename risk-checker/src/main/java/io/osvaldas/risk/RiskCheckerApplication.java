package io.osvaldas.risk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(
    scanBasePackages = {
        "io.osvaldas.api",
        "io.osvaldas.risk"
    }
)
@ConfigurationPropertiesScan
public class RiskCheckerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskCheckerApplication.class, args);
    }

}
