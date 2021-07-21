package com.finance.interest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InterestApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterestApplication.class, args);
    }

}
