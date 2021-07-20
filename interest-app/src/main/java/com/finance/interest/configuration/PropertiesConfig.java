package com.finance.interest.configuration;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "application")
public class PropertiesConfig {

    private BigDecimal maxAmount;

    private Double interestRate;

    private int postponeDays;

    private int requestsFromSameIpLimit;

    @Min(value = 0)
    @Max(value = 24)
    private int forbiddenHourFrom;

    @Min(value = 0)
    @Max(value = 24)
    private int forbiddenHourTo;

    private double interestIncrementFactor;
}