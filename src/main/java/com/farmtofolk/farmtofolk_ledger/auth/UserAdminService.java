package com.farmtofolk.farmtofolk_ledger.auth;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserAdminService {

  private final UserRepository userRepository;
  private final FarmerRepository farmerRepository;
  private final PasswordEncoder passwordEncoder;

  public UserAdminService(
      UserRepository userRepository,
      FarmerRepository farmerRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.farmerRepository = farmerRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse createUser(CreateUserRequest request) {
    UserRole role = request.role();
    if (role != UserRole.FIELD_OFFICER && role != UserRole.FARMER) {
      throw new BadRequestException("Role must be FIELD_OFFICER or FARMER");
    }

    String email = normalize(request.email());
    String phone = normalize(request.phone());
    validateContactAndDuplicates(email, phone);

    Farmer farmer = null;
    if (role == UserRole.FARMER) {
      if (request.farmerId() == null) {
        throw new BadRequestException("farmerId is required for FARMER users");
      }
      farmer =
          farmerRepository
              .findById(request.farmerId())
              .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
      if (farmer.getUserId() != null) {
        throw new ConflictException("Farmer already has a linked user");
      }
    }

    User user = new User();
    user.setName(request.name());
    user.setEmail(email);
    user.setPhone(phone);
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(role);
    user.setGender(request.gender());
    user.setAddress(request.address());
    user.setActive(true);
    User savedUser = userRepository.save(user);

    if (farmer != null) {
      // Credentials and farmer business data stay separate, joined only by this UUID.
      farmer.setUserId(savedUser.getId());
      farmerRepository.save(farmer);
    }
    return UserResponse.from(savedUser);
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserResponse> getUsers() {
    return userRepository.findAll().stream().map(UserResponse::from).toList();
  }

  @PreAuthorize("hasRole('ADMIN')")
  public UserResponse updateStatus(java.util.UUID userId, UpdateUserStatusRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setActive(request.active());
    return UserResponse.from(userRepository.save(user));
  }

  private void validateContactAndDuplicates(String email, String phone) {
    if (email == null && phone == null) {
      throw new BadRequestException("Email or phone is required");
    }
    if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
      throw new ConflictException("Email already exists");
    }
    if (phone != null && userRepository.existsByPhone(phone)) {
      throw new ConflictException("Phone already exists");
    }
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
