package com.farmtofolk.farmtofolk_ledger.auth;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

  private final UserRepository userRepository;

  public CurrentUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Optional<UUID> getCurrentUserIdOptional() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return Optional.empty();
    }
    if (authentication.getPrincipal() instanceof User user) {
      return Optional.ofNullable(user.getId());
    }
    String authenticationName = authentication.getName();
    if (authenticationName == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(UUID.fromString(authenticationName));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

  public UUID getCurrentUserId() {
    return getCurrentUserIdOptional()
        .orElseThrow(
            () ->
                new org.springframework.security.access.AccessDeniedException(
                    "Authentication required"));
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof User user) {
      return user;
    }
    return userRepository
        .findById(getCurrentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}
