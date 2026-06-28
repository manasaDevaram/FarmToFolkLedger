package com.farmtofolk.farmtofolk_ledger.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.farmtofolk.farmtofolk_ledger.common.error.UnauthorizedException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtService jwtService;

  @Test
  void loginWorksWithEmail() {
    User user = activeUser();
    when(userRepository.findByEmailIgnoreCase("farmer@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "hash")).thenReturn(true);
    when(jwtService.generateToken(user)).thenReturn("token");

    LoginResponse response = service().login(new LoginRequest("farmer@example.com", "password"));

    assertEquals("token", response.token());
    assertEquals(UserRole.FARMER, response.user().role());
  }

  @Test
  void loginWorksWithPhone() {
    User user = activeUser();
    when(userRepository.findByEmailIgnoreCase("9876543210")).thenReturn(Optional.empty());
    when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "hash")).thenReturn(true);
    when(jwtService.generateToken(user)).thenReturn("token");

    LoginResponse response = service().login(new LoginRequest("9876543210", "password"));

    assertEquals("token", response.token());
  }

  @Test
  void loginRejectsInactiveUser() {
    User user = new User();
    user.setPasswordHash("hash");
    user.setActive(false);
    when(userRepository.findByEmailIgnoreCase("farmer@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "hash")).thenReturn(true);

    UnauthorizedException exception =
        assertThrows(
            UnauthorizedException.class,
            () -> service().login(new LoginRequest("farmer@example.com", "password")));
    assertEquals("User account is inactive", exception.getMessage());
    verifyNoInteractions(jwtService);
  }

  private AuthService service() {
    return new AuthService(userRepository, passwordEncoder, jwtService);
  }

  private User activeUser() {
    User user = new User();
    user.setName("Farmer");
    user.setPasswordHash("hash");
    user.setRole(UserRole.FARMER);
    user.setActive(true);
    return user;
  }
}
