package com.farmtofolk.farmtofolk_ledger.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

  @Mock UserRepository userRepository;
  @Mock FarmerRepository farmerRepository;
  @Mock PasswordEncoder passwordEncoder;

  @Test
  void farmerUserIsHashedAndLinkedToFarmerProfile() {
    UUID farmerId = UUID.randomUUID();
    Farmer farmer = new Farmer();
    when(farmerRepository.findById(farmerId)).thenReturn(Optional.of(farmer));
    when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
              return user;
            });
    UserAdminService service =
        new UserAdminService(userRepository, farmerRepository, passwordEncoder);

    service.createUser(
        new CreateUserRequest(
            "Ramesh",
            "ramesh@example.com",
            "9876543210",
            "password123",
            UserRole.FARMER,
            "MALE",
            "Mysuru",
            farmerId));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertEquals("bcrypt-hash", userCaptor.getValue().getPasswordHash());
    assertEquals(userCaptor.getValue().getId(), farmer.getUserId());
    verify(farmerRepository).save(farmer);
  }
}
