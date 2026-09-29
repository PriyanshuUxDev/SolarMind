package com.solarmind.config;
import jakarta.validation.constraints.NotBlank; import org.springframework.boot.context.properties.ConfigurationProperties; import org.springframework.validation.annotation.Validated; import java.time.Duration;
@Validated @ConfigurationProperties("solarmind.ai") public record AiProperties(@NotBlank String serviceUrl, Duration timeout, @NotBlank String internalToken) {}

