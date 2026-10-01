package com.solarmind.security;

import com.solarmind.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository users;

  public CustomUserDetailsService(UserRepository users) {
    this.users = users;
  }

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var u =
        users
            .findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return User.withUsername(u.getEmail()).password(u.getPassword()).authorities("USER").build();
  }
}
