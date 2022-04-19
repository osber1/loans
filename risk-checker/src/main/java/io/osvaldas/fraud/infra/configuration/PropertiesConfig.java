package io.osvaldas.fraud.infra.configuration;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "application")
public class PropertiesConfig {

    @NotNull
    @Min(value = 0)
    private BigDecimal maxAmount;

    @NotNull
    @Min(value = 0)
    @Max(value = 24)
    private int forbiddenHourFrom;

    @NotNull
    @Min(value = 0)
    @Max(value = 24)
    private int forbiddenHourTo;

    @NotNull
    private int loanLimitPerDay;

}
