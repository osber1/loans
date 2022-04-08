package io.osvaldas.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(
    scanBasePackages = {
        "io.osvaldas.backoffice",
        "io.osvaldas.messages",
    }
)
@ConfigurationPropertiesScan
public class BackOfficeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackOfficeApplication.class, args);
    }

}
