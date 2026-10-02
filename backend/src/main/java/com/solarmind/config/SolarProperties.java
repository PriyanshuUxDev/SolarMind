package com.solarmind.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("solarmind.assumptions")
public record SolarProperties(
    @NotNull @DecimalMin(value = "0.000001") BigDecimal generationFactorKwhPerKwYear,
    @NotNull @DecimalMin(value = "0.000001") BigDecimal installationCostPerKw,
    @NotNull @DecimalMin(value = "0") BigDecimal gridEmissionFactor,
    @NotNull @DecimalMin(value = "0.000001") BigDecimal roofLayoutFactor,
    @NotNull @NotEmpty Map<String, BigDecimal> roofTypeUsableFactor,
    @NotNull @Min(0) Integer systemLifespanYears,
    @NotNull @DecimalMin(value = "0") BigDecimal tariffEscalationRate,
    @NotNull @DecimalMin(value = "0") BigDecimal panelDegradationRate) {}
