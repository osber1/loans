package io.osvaldas.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableCaching
@EnableFeignClients
@SpringBootApplication(
    scanBasePackages = {
        "io.osvaldas.backoffice",
        "io.osvaldas.messages",
        "io.osvaldas.api"
    }
)
@ConfigurationPropertiesScan(
    basePackages = {
        "io.osvaldas.backoffice",
        "io.osvaldas.messages"
    })
@EnableJpaRepositories(repositoryFactoryBeanClass = EnversRevisionRepositoryFactoryBean.class)
public class BackOfficeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackOfficeApplication.class, args);
    }

}

// TODO add new tables to the database
