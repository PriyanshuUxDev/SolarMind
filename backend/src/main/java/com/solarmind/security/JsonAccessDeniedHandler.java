package com.solarmind.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class JsonAccessDeniedHandler implements AccessDeniedHandler {
  public void handle(HttpServletRequest r, HttpServletResponse s, AccessDeniedException e)
      throws IOException {
    s.setStatus(403);
    s.setContentType("application/json");
    s.getWriter()
        .write(
            "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\",\"path\":\""
                + r.getRequestURI()
                + "\"}");
  }
}
