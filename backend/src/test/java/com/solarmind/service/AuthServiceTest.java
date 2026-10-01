package com.solarmind.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.solarmind.config.JwtProperties;
import com.solarmind.dto.request.AuthRequests.RegisterRequest;
import com.solarmind.entity.User;
import com.solarmind.repository.UserRepository;
import com.solarmind.security.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
  @Test
  void registrationStoresEmailAndPasswordHashNotPlaintextPassword() {
    UserRepository users = mock(UserRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    JwtUtil jwt =
        new JwtUtil(
            new JwtProperties("test-only-secret-that-is-at-least-32-characters-long", 3600000L));
    AuthService service = new AuthService(users, encoder, jwt);
    when(users.findByEmail("asha@example.com")).thenReturn(Optional.empty());
    when(encoder.encode("secret123")).thenReturn("$2a$encoded-password-hash");
    when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.register(new RegisterRequest("Asha", " ASHA@example.com ", "secret123", "secret123"));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(users).save(captor.capture());
    assertEquals("asha@example.com", captor.getValue().getEmail());
    assertEquals("$2a$encoded-password-hash", captor.getValue().getPassword());
    assertNotEquals("secret123", captor.getValue().getPassword());
  }
}
