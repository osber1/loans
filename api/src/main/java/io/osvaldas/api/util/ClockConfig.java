package io.osvaldas.api.util;

import static java.time.Clock.systemUTC;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return systemUTC();
    }
}
