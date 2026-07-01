package com.farmtofolk.farmtofolk_ledger.auth;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final CurrentUserService currentUserService;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      CurrentUserService currentUserService,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.currentUserService = currentUserService;
    this.passwordEncoder = passwordEncoder;
  }

  @PreAuthorize("isAuthenticated()")
  public PasswordChangeResponse changeOwnPassword(ChangePasswordRequest request) {
    User user =
        userRepository
            .findById(currentUserService.getCurrentUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    validateMatchingPasswords(request.newPassword(), request.confirmPassword());
    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new BadRequestException("Current password is incorrect");
    }
    if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
      throw new BadRequestException("New password must be different from current password");
    }

    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    User savedUser = userRepository.save(user);
    return PasswordChangeResponse.from("Password changed successfully", savedUser);
  }

  static void validateMatchingPasswords(String newPassword, String confirmPassword) {
    if (!newPassword.equals(confirmPassword)) {
      throw new BadRequestException("New password and confirm password do not match");
    }
  }
}
