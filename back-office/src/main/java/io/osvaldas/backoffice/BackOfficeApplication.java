package io.osvaldas.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(
    scanBasePackages = {
        "io.osvaldas.backoffice",
        "io.osvaldas.messages",
        "io.osvaldas.api"
    }
)
@ConfigurationPropertiesScan
public class BackOfficeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackOfficeApplication.class, args);
    }

}
