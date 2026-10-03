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

    /**
     * First hour (inclusive, 0-23) of the forbidden window.
     */
    @NotNull
    @Min(0)
    @Max(23)
    private Integer forbiddenHourFrom;

    /**
     * Hour (exclusive, 0-23) at which the forbidden window ends. When it is lower than
     * {@link #forbiddenHourFrom} the window wraps around midnight (e.g. 22 to 6); when equal, the window is empty.
     */
    @NotNull
    @Min(0)
    @Max(23)
    private Integer forbiddenHourTo;

    @NotNull
    @Positive
    private Integer loanLimitPerDay;

}
