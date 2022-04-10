package io.osvaldas.backoffice.infra.configuration;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
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

    @Min(value = 0)
    private BigDecimal maxAmount;

    @Min(value = 0)
    private BigDecimal interestRate;

    private int postponeDays;

    private int requestsFromSameIpLimit;

    @Min(value = 0)
    @Max(value = 24)
    private int forbiddenHourFrom;

    @Min(value = 0)
    @Max(value = 24)
    private int forbiddenHourTo;

    private BigDecimal interestIncrementFactor;
}
