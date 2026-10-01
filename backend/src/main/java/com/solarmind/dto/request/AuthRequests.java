package com.solarmind.dto.request;

import jakarta.validation.constraints.*;

public final class AuthRequests {
  private AuthRequests() {}

  public record RegisterRequest(
      @NotBlank String name,
      @NotBlank @Email String email,
      @NotBlank @Size(min = 8) String password,
      @NotBlank String confirmPassword) {}

  public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
}
