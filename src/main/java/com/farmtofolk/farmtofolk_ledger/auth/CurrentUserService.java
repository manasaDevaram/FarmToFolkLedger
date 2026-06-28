package com.farmtofolk.farmtofolk_ledger.auth;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UUID> getCurrentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(authentication.getName()));
    }

    public UUID getCurrentUserId() {
        return getCurrentUserIdOptional()
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Authentication required"));
    }

    public User getCurrentUser() {
        return userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
