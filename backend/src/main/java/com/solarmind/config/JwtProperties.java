package com.solarmind.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.nio.charset.StandardCharsets;

@Validated
@ConfigurationProperties("solarmind.jwt")
public record JwtProperties(@NotBlank String secret, @NotNull @Positive Long expirationMs) {
  public JwtProperties {
    if (secret != null && secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalArgumentException("solarmind.jwt.secret must be at least 32 bytes");
    }
  }
}
