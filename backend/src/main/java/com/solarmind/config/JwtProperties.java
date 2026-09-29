package com.solarmind.config;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull; import org.springframework.boot.context.properties.ConfigurationProperties; import org.springframework.validation.annotation.Validated;
@Validated @ConfigurationProperties("solarmind.jwt") public record JwtProperties(@NotBlank String secret, @NotNull Long expirationMs) {}

