package com.farmtofolk.farmtofolk_ledger.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private CurrentUserService currentUserService;

  private AdminUserService service;

  @BeforeEach
  void setUp() {
    service =
        new AdminUserService(
            userRepository, passwordEncoder, currentUserService, "ChangeMe@123");
  }

  @Test
  void createsAdminAndFieldOfficerWithoutExposingPassword() {
    when(passwordEncoder.encode("temporary123")).thenReturn("encoded");
    when(passwordEncoder.encode("ChangeMe@123")).thenReturn("default-encoded");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    for (UserRole role : List.of(UserRole.ADMIN, UserRole.FIELD_OFFICER)) {
      InternalUserResponse response =
          service.create(
              new CreateInternalUserRequest(
                  "Internal User",
                  role.name().toLowerCase() + "@example.com",
                  role == UserRole.ADMIN ? "9876543210" : "9876543211",
                  role,
                  true,
                  "temporary123"));
      assertEquals(role, response.role());
      assertTrue(response.active());
    }

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, org.mockito.Mockito.times(2)).save(captor.capture());
    assertEquals("encoded", captor.getAllValues().getFirst().getPasswordHash());
    assertEquals("default-encoded", captor.getAllValues().getLast().getPasswordHash());
  }

  @Test
  void rejectsFarmerRole() {
    CreateInternalUserRequest request =
        new CreateInternalUserRequest(
            "Farmer", "farmer@example.com", "9876543210", UserRole.FARMER, true, "password123");

    assertThrows(BadRequestException.class, () -> service.create(request));
    verify(userRepository, never()).save(any());
  }

  @Test
  void rejectsDuplicateEmailAndPhone() {
    CreateInternalUserRequest request =
        new CreateInternalUserRequest(
            "Admin", "admin@example.com", "9876543210", UserRole.ADMIN, true, "password123");
    when(userRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.create(request));

    when(userRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(false);
    when(userRepository.existsByPhone("9876543210")).thenReturn(true);
    assertThrows(ConflictException.class, () -> service.create(request));
  }

  @Test
  void listsOnlyInternalRolesRequestedFromRepository() {
    User admin = user("Admin", UserRole.ADMIN);
    User officer = user("Officer", UserRole.FIELD_OFFICER);
    when(userRepository.findByRoleIn(any())).thenReturn(List.of(officer, admin));

    List<InternalUserResponse> responses = service.list();

    assertEquals(List.of(UserRole.ADMIN, UserRole.FIELD_OFFICER), responses.stream().map(InternalUserResponse::role).toList());
    ArgumentCaptor<java.util.Collection<UserRole>> roles = ArgumentCaptor.forClass(java.util.Collection.class);
    verify(userRepository).findByRoleIn(roles.capture());
    assertEquals(3, roles.getValue().size());
    assertTrue(roles.getValue().contains(UserRole.FARMER));
  }

  @Test
  void rejectsChangingOwnRoleOrStatus() {
    java.util.UUID currentUserId = java.util.UUID.randomUUID();
    when(currentUserService.getCurrentUserId()).thenReturn(currentUserId);

    BadRequestException roleException =
        assertThrows(
            BadRequestException.class,
            () ->
                service.updateRole(
                    currentUserId, new UpdateInternalUserRoleRequest(UserRole.FIELD_OFFICER)));
    BadRequestException statusException =
        assertThrows(
            BadRequestException.class,
            () -> service.updateStatus(currentUserId, new UpdateUserStatusRequest(false)));

    assertEquals("You cannot change your own role/status", roleException.getMessage());
    assertEquals("You cannot change your own role/status", statusException.getMessage());
    verify(userRepository, never()).findById(any());
  }

  @Test
  void adminResetsAnotherUsersPasswordUsingHash() {
    UUID adminId = UUID.randomUUID();
    UUID farmerId = UUID.randomUUID();
    User farmer = user("Farmer", UserRole.FARMER);
    when(currentUserService.getCurrentUserId()).thenReturn(adminId);
    when(userRepository.findById(farmerId)).thenReturn(Optional.of(farmer));
    when(passwordEncoder.encode("ResetPassword@2")).thenReturn("reset-hash");
    when(userRepository.save(farmer)).thenReturn(farmer);

    PasswordChangeResponse response =
        service.resetUserPassword(
            farmerId, new AdminResetPasswordRequest("ResetPassword@2", "ResetPassword@2"));

    assertEquals("reset-hash", farmer.getPasswordHash());
    assertEquals("Password reset successfully", response.message());
  }

  @Test
  void adminCannotResetOwnPasswordThroughAdminEndpoint() {
    UUID adminId = UUID.randomUUID();
    when(currentUserService.getCurrentUserId()).thenReturn(adminId);

    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                service.resetUserPassword(
                    adminId,
                    new AdminResetPasswordRequest("ResetPassword@2", "ResetPassword@2")));

    assertEquals("Use change password to update your own password", exception.getMessage());
    verify(userRepository, never()).findById(any());
  }

  private User user(String name, UserRole role) {
    User user = new User();
    user.setName(name);
    user.setEmail(name.toLowerCase() + "@example.com");
    user.setPhone(role == UserRole.ADMIN ? "9876543210" : "9876543211");
    user.setRole(role);
    user.setActive(true);
    user.setPasswordHash("encoded");
    return user;
  }
}
