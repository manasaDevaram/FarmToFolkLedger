package com.farmtofolk.farmtofolk_ledger.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private CurrentUserService currentUserService;
  @Mock private PasswordEncoder passwordEncoder;

  private UserService service;
  private UUID userId;
  private User user;

  @BeforeEach
  void setUp() {
    service = new UserService(userRepository, currentUserService, passwordEncoder);
    userId = UUID.randomUUID();
    user = new User();
    user.setName("User");
    user.setPhone("9876543210");
    user.setRole(UserRole.FARMER);
    user.setPasswordHash("old-hash");
    when(currentUserService.getCurrentUserId()).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
  }

  @Test
  void changesOwnPasswordUsingHash() {
    when(passwordEncoder.matches("Current@1", "old-hash")).thenReturn(true);
    when(passwordEncoder.matches("NewPassword@2", "old-hash")).thenReturn(false);
    when(passwordEncoder.encode("NewPassword@2")).thenReturn("new-hash");
    when(userRepository.save(user)).thenReturn(user);

    PasswordChangeResponse response =
        service.changeOwnPassword(
            new ChangePasswordRequest("Current@1", "NewPassword@2", "NewPassword@2"));

    assertEquals("new-hash", user.getPasswordHash());
    assertEquals("Password changed successfully", response.message());
  }

  @Test
  void rejectsWrongCurrentPassword() {
    when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                service.changeOwnPassword(
                    new ChangePasswordRequest("wrong", "NewPassword@2", "NewPassword@2")));

    assertEquals("Current password is incorrect", exception.getMessage());
    verify(userRepository, never()).save(user);
  }

  @Test
  void rejectsMismatchedConfirmation() {
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                service.changeOwnPassword(
                    new ChangePasswordRequest("Current@1", "NewPassword@2", "Different@3")));

    assertEquals("New password and confirm password do not match", exception.getMessage());
  }
}
