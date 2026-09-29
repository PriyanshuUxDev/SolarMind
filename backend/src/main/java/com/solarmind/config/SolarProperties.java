package com.solarmind.config;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;
import java.util.Map;
@Validated @ConfigurationProperties("solarmind.assumptions")
public record SolarProperties(@NotNull BigDecimal generationFactorKwhPerKwYear, @NotNull BigDecimal installationCostPerKw, @NotNull BigDecimal gridEmissionFactor, @NotNull BigDecimal roofLayoutFactor, @NotNull Map<String, BigDecimal> roofTypeUsableFactor) {}

