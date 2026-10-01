package com.solarmind.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
  public void commence(HttpServletRequest r, HttpServletResponse s, AuthenticationException e)
      throws IOException {
    s.setStatus(401);
    s.setContentType("application/json");
    s.getWriter()
        .write(
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication"
                + " required\",\"path\":\""
                + r.getRequestURI()
                + "\"}");
  }
}
