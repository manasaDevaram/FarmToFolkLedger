package com.farmtofolk.farmtofolk_ledger.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock ApplicationArguments applicationArguments;

  @Test
  void enabledBootstrapCreatesOneBcryptReadyAdminWhenNoneExists() {
    when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
    AdminBootstrap bootstrap =
        new AdminBootstrap(
            userRepository,
            passwordEncoder,
            true,
            "Initial Admin",
            "9999999999",
            "",
            "password123");

    bootstrap.run(applicationArguments);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertEquals(UserRole.ADMIN, userCaptor.getValue().getRole());
    assertEquals("bcrypt-hash", userCaptor.getValue().getPasswordHash());
  }

  @Test
  void bootstrapDoesNothingWhenAdminAlreadyExists() {
    when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);
    AdminBootstrap bootstrap =
        new AdminBootstrap(
            userRepository, passwordEncoder, true, "Admin", "9999999999", "", "password123");

    bootstrap.run(applicationArguments);

    verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void bootstrapRejectsWeakPassword() {
    when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
    AdminBootstrap bootstrap =
        new AdminBootstrap(
            userRepository, passwordEncoder, true, "Admin", "9999999999", "", "short");

    assertThrows(IllegalStateException.class, () -> bootstrap.run(applicationArguments));
  }
}
