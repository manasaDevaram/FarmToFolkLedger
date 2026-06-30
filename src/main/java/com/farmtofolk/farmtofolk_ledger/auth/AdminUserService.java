package com.farmtofolk.farmtofolk_ledger.auth;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserService {

  private static final Set<UserRole> INTERNAL_ROLES =
      EnumSet.of(UserRole.ADMIN, UserRole.FIELD_OFFICER);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public InternalUserResponse create(CreateInternalUserRequest request) {
    validateInternalRole(request.role());
    String email = normalizeEmail(request.email());
    String phone = normalize(request.phone());
    validateUniqueContacts(email, phone, null);

    User user = new User();
    user.setName(request.name().trim());
    user.setEmail(email);
    user.setPhone(phone);
    user.setRole(request.role());
    user.setActive(request.active() == null || request.active());
    user.setPasswordHash(passwordEncoder.encode(request.initialPassword()));
    return InternalUserResponse.from(userRepository.save(user));
  }

  @Transactional(readOnly = true)
  public List<InternalUserResponse> list() {
    return userRepository.findByRoleIn(INTERNAL_ROLES).stream()
        .sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER))
        .map(InternalUserResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public InternalUserResponse get(UUID userId) {
    return InternalUserResponse.from(findInternalUser(userId));
  }

  public InternalUserResponse update(UUID userId, UpdateInternalUserRequest request) {
    User user = findInternalUser(userId);
    String email = request.email() == null ? user.getEmail() : normalizeEmail(request.email());
    String phone = request.phone() == null ? user.getPhone() : normalize(request.phone());
    validateUniqueContacts(email, phone, userId);

    if (request.name() != null) {
      if (request.name().isBlank()) throw new BadRequestException("Name must not be blank");
      user.setName(request.name().trim());
    }
    if (request.email() != null) user.setEmail(email);
    if (request.phone() != null) user.setPhone(phone);
    if (request.active() != null) user.setActive(request.active());
    return InternalUserResponse.from(userRepository.save(user));
  }

  public InternalUserResponse updateRole(UUID userId, UpdateInternalUserRoleRequest request) {
    validateInternalRole(request.role());
    User user = findInternalUser(userId);
    user.setRole(request.role());
    return InternalUserResponse.from(userRepository.save(user));
  }

  public InternalUserResponse updateStatus(UUID userId, UpdateUserStatusRequest request) {
    User user = findInternalUser(userId);
    user.setActive(request.active());
    return InternalUserResponse.from(userRepository.save(user));
  }

  private User findInternalUser(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (!INTERNAL_ROLES.contains(user.getRole())) {
      throw new ResourceNotFoundException("User not found");
    }
    return user;
  }

  private void validateInternalRole(UserRole role) {
    if (!INTERNAL_ROLES.contains(role)) {
      throw new BadRequestException("Invalid role for internal user creation");
    }
  }

  private void validateUniqueContacts(String email, String phone, UUID userId) {
    boolean duplicateEmail =
        userId == null
            ? userRepository.existsByEmailIgnoreCase(email)
            : userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId);
    if (duplicateEmail) throw new ConflictException("Email already exists");

    boolean duplicatePhone =
        userId == null
            ? userRepository.existsByPhone(phone)
            : userRepository.existsByPhoneAndIdNot(phone, userId);
    if (duplicatePhone) throw new ConflictException("Phone already exists");
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String normalize(String value) {
    return value.trim();
  }
}
