package com.farmtofolk.farmtofolk_ledger.farmer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.auth.User;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.auth.UserRepository;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class FarmerServiceTest {

  @Mock FarmerRepository farmerRepository;
  @Mock PublicTraceCacheService cacheService;
  @Mock StorageService storageService;
  @Mock AfterCommitExecutor afterCommitExecutor;
  @Mock PlatformTransactionManager transactionManager;
  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;

  @Test
  void createRejectsDuplicateFarmerPhone() {
    when(farmerRepository.existsByPhone("9876543210")).thenReturn(true);
    FarmerService service =
        new FarmerService(
            farmerRepository,
            cacheService,
            storageService,
            afterCommitExecutor,
            transactionManager,
            userRepository,
            passwordEncoder,
            "ChangeMe@123");

    assertThrows(
        ConflictException.class,
        () ->
            service.createFarmer(
                new CreateFarmerRequest(
                    "FTF-FR-2026-000001",
                    "Ramesh",
                    "9876543210",
                    "Hullahalli",
                    "Mysuru",
                    "Karnataka",
                    null,
                    null,
                    null,
                    LocalDate.now())));
  }

  @Test
  void createAlsoProvisionsFarmerLoginWithDefaultPassword() {
    UUID userId = UUID.randomUUID();
    User savedUser = org.mockito.Mockito.mock(User.class);
    when(savedUser.getId()).thenReturn(userId);
    when(passwordEncoder.encode("ChangeMe@123")).thenReturn("encoded-default");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(farmerRepository.save(any(Farmer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    FarmerService service =
        new FarmerService(
            farmerRepository,
            cacheService,
            storageService,
            afterCommitExecutor,
            transactionManager,
            userRepository,
            passwordEncoder,
            "ChangeMe@123");

    service.createFarmer(
        new CreateFarmerRequest(
            "FTF-FR-2026-000002",
            "Ramesh",
            "9876543210",
            "Hullahalli",
            "Mysuru",
            "Karnataka",
            null,
            null,
            null,
            LocalDate.now()));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    ArgumentCaptor<Farmer> farmerCaptor = ArgumentCaptor.forClass(Farmer.class);
    verify(userRepository).save(userCaptor.capture());
    verify(farmerRepository).save(farmerCaptor.capture());
    assertEquals("encoded-default", userCaptor.getValue().getPasswordHash());
    assertEquals(com.farmtofolk.farmtofolk_ledger.auth.UserRole.FARMER, userCaptor.getValue().getRole());
    assertEquals(userId, farmerCaptor.getValue().getUserId());
  }
}
