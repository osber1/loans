package io.osvaldas.risk.infra.configuration;

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
    @Min(0)
    private BigDecimal maxAmount;

    @NotNull
    @Min(0)
    @Max(24)
    private int forbiddenHourFrom;

    @NotNull
    @Min(0)
    @Max(24)
    private int forbiddenHourTo;

    @NotNull
    private int loanLimitPerDay;

}
