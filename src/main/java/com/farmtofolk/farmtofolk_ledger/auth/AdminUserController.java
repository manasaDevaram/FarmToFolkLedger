package com.farmtofolk.farmtofolk_ledger.auth;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;

  public AdminUserController(AdminUserService adminUserService) {
    this.adminUserService = adminUserService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public InternalUserResponse create(@Valid @RequestBody CreateInternalUserRequest request) {
    return adminUserService.create(request);
  }

  @GetMapping
  public List<InternalUserResponse> list() {
    return adminUserService.list();
  }

  @GetMapping("/{userId}")
  public InternalUserResponse get(@PathVariable UUID userId) {
    return adminUserService.get(userId);
  }

  @PatchMapping("/{userId}")
  public InternalUserResponse update(
      @PathVariable UUID userId, @Valid @RequestBody UpdateInternalUserRequest request) {
    return adminUserService.update(userId, request);
  }

  @PatchMapping("/{userId}/role")
  public InternalUserResponse updateRole(
      @PathVariable UUID userId, @Valid @RequestBody UpdateInternalUserRoleRequest request) {
    return adminUserService.updateRole(userId, request);
  }

  @PatchMapping("/{userId}/status")
  public InternalUserResponse updateStatus(
      @PathVariable UUID userId, @Valid @RequestBody UpdateUserStatusRequest request) {
    return adminUserService.updateStatus(userId, request);
  }

  @PatchMapping("/{userId}/password")
  public PasswordChangeResponse resetPassword(
      @PathVariable UUID userId, @Valid @RequestBody AdminResetPasswordRequest request) {
    return adminUserService.resetUserPassword(userId, request);
  }
}
