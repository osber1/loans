package io.osvaldas.risk.infra.configuration;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
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
