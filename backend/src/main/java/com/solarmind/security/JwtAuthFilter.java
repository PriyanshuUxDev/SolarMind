package com.solarmind.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtil jwt;

  public JwtAuthFilter(JwtUtil jwt) {
    this.jwt = jwt;
  }

  protected void doFilterInternal(HttpServletRequest r, HttpServletResponse s, FilterChain c)
      throws ServletException, IOException {
    String h = r.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ") && jwt.valid(h.substring(7))) {
      var a = new UsernamePasswordAuthenticationToken(jwt.subject(h.substring(7)), null, List.of());
      SecurityContextHolder.getContext().setAuthentication(a);
    }
    c.doFilter(r, s);
  }
}
