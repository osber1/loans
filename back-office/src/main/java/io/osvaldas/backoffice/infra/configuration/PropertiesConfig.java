package io.osvaldas.backoffice.infra.configuration;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @Positive
    private Integer postponeDays;

    /**
     * Multiplier applied to the interest rate on every postpone, so it must not decrease the rate.
     */
    @NotNull
    @DecimalMin("1")
    private BigDecimal interestIncrementFactor;

}
