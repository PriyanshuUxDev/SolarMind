package com.solarmind.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 191)
  private String name;

  @Column(nullable = false, unique = true, length = 191)
  private String email;

  @Column(nullable = false, length = 191)
  private String password;

  private Instant createdAt = Instant.now();

  protected User() {}

  public User(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
