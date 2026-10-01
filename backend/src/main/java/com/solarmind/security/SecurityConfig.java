package com.solarmind.security;

import com.solarmind.config.CorsProperties;
import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
  private final JwtAuthFilter filter;
  private final CorsProperties cors;

  public SecurityConfig(JwtAuthFilter f, CorsProperties c) {
    filter = f;
    cors = c;
  }

  @Bean
  org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
    return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain chain(HttpSecurity h) throws Exception {
    return h.csrf(c -> c.disable())
        .cors(
            c ->
                c.configurationSource(
                    x -> {
                      var q = new CorsConfiguration();
                      q.setAllowedOrigins(
                          java.util.Arrays.stream(cors.allowedOrigin().split(","))
                              .map(String::trim)
                              .filter(v -> !v.isEmpty())
                              .toList());
                      q.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                      q.setAllowedHeaders(List.of("*"));
                      return q;
                    }))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(new JsonAuthenticationEntryPoint())
                    .accessDeniedHandler(new JsonAccessDeniedHandler()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/auth/**", "/api/health", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
