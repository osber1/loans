package io.osvaldas.backoffice.infra.configuration.feign;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.json.JsonMapper.builder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class CustomFeignClientConfiguration {

    @Bean
    public FeignErrorDecoder errorDecoder() {
        JsonMapper mapper = builder()
            .addModule(new JavaTimeModule())
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(WRITE_DATE_KEYS_AS_TIMESTAMPS)
            .build();
        return new FeignErrorDecoder(mapper);
    }
}
