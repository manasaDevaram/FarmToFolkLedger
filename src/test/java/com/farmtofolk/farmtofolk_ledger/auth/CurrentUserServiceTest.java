package com.farmtofolk.farmtofolk_ledger.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

  @Mock UserRepository userRepository;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void invalidAuthenticationNameReturnsEmpty() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("not-a-uuid", null, List.of()));

    assertTrue(new CurrentUserService(userRepository).getCurrentUserIdOptional().isEmpty());
  }

  @Test
  void userPrincipalAvoidsRepeatedRepositoryLookup() {
    UUID userId = UUID.randomUUID();
    User user = new User();
    ReflectionTestUtils.setField(user, "id", userId);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    CurrentUserService service = new CurrentUserService(userRepository);

    assertEquals(userId, service.getCurrentUserId());
    assertEquals(user, service.getCurrentUser());
    verifyNoInteractions(userRepository);
  }
}
