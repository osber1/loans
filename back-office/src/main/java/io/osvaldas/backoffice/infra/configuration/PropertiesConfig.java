package io.osvaldas.backoffice.infra.configuration;

import java.math.BigDecimal;

import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application")
public class PropertiesConfig {

    // TODO sudet not empty not null ant visu properciu

    @Min(value = 0)
    private BigDecimal interestRate;

    @Min(value = 0)
    private int postponeDays;

    @Min(value = 0)
    private BigDecimal interestIncrementFactor;

}
