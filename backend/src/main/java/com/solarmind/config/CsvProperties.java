package com.solarmind.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("solarmind.csv")
public record CsvProperties(
    boolean importEnabled, @NotBlank String panelsPath, @NotBlank String locationsPath) {}
