package com.farmtofolk.farmtofolk_ledger.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  @Test
  void generatedTokenContainsUserIdentity() {
    UUID userId = UUID.randomUUID();
    User user = new User();
    ReflectionTestUtils.setField(user, "id", userId);
    user.setRole(UserRole.FARMER);
    JwtService jwtService =
        new JwtService("test-secret-must-be-at-least-thirty-two-bytes-long", 60);

    String token = jwtService.generateToken(user);

    assertEquals(userId, jwtService.extractUserId(token));
  }
}
