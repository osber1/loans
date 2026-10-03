package io.osvaldas.api.util;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(@Value("${application.timeZone:Europe/Vilnius}") ZoneId timeZone) {
        return Clock.system(timeZone);
    }
}
