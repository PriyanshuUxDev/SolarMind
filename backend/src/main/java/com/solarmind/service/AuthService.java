package com.solarmind.service;

import com.solarmind.dto.request.AuthRequests.*;
import com.solarmind.dto.response.AuthResponse;
import com.solarmind.entity.User;
import com.solarmind.exception.InvalidInputException;
import com.solarmind.exception.InvalidCredentialsException;
import com.solarmind.repository.UserRepository;
import com.solarmind.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;

  public AuthService(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  public AuthResponse register(RegisterRequest r) {
    String email = r.email().trim().toLowerCase();
    if (!r.password().equals(r.confirmPassword()))
      throw new InvalidInputException("Passwords do not match");
    if (users.findByEmail(email).isPresent())
      throw new InvalidInputException("Email is already registered");
    User u = users.save(new User(r.name().trim(), email, encoder.encode(r.password())));
    return new AuthResponse(
        jwt.issue(u.getEmail()),
        new AuthResponse.UserResponse(u.getId(), u.getName(), u.getEmail()));
  }

  public AuthResponse login(LoginRequest r) {
    User u =
        users
            .findByEmail(r.email().trim().toLowerCase())
            .orElseThrow(InvalidCredentialsException::new);
    if (!encoder.matches(r.password(), u.getPassword()))
      throw new InvalidCredentialsException();
    return new AuthResponse(
        jwt.issue(u.getEmail()),
        new AuthResponse.UserResponse(u.getId(), u.getName(), u.getEmail()));
  }
}
