package com.solarmind.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
  private CurrentUser() {}

  public static String subject() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }
}
