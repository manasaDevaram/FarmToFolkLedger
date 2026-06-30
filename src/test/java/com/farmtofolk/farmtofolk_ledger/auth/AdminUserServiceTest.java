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

  private AdminUserService service;

  @BeforeEach
  void setUp() {
    service = new AdminUserService(userRepository, passwordEncoder);
  }

  @Test
  void createsAdminAndFieldOfficerWithoutExposingPassword() {
    when(passwordEncoder.encode("temporary123")).thenReturn("encoded");
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
    assertTrue(captor.getAllValues().stream().allMatch(user -> "encoded".equals(user.getPasswordHash())));
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
    assertEquals(2, roles.getValue().size());
    assertFalse(roles.getValue().contains(UserRole.FARMER));
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
