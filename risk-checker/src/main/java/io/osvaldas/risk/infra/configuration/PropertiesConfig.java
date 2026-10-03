package io.osvaldas.risk.infra.configuration;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
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
    private BigDecimal maxAmount;

    @NotNull
    @Min(0)
    @Max(23)
    private Integer forbiddenHourFrom;

    @NotNull
    @Min(0)
    @Max(23)
    private Integer forbiddenHourTo;

    @NotNull
    @Positive
    private Integer loanLimitPerDay;

}
