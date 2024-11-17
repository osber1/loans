package io.osvaldas.backoffice.infra.configuration;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application")
public class PropertiesConfig {

    @NotNull
    @Min(0)
    private BigDecimal interestRate;

    @NotNull
    @Min(0)
    private Integer postponeDays;

    @NotNull
    @Min(0)
    private BigDecimal interestIncrementFactor;

}
