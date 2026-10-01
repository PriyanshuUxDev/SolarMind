package com.solarmind.dto.response;

public record AuthResponse(String token, UserResponse user) {
  public record UserResponse(Long id, String name, String email) {}
}
