package com.farmtofolk.farmtofolk_ledger.farmer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
class FarmerServiceTest {

  @Mock FarmerRepository farmerRepository;
  @Mock PublicTraceCacheService cacheService;
  @Mock StorageService storageService;
  @Mock AfterCommitExecutor afterCommitExecutor;
  @Mock PlatformTransactionManager transactionManager;

  @Test
  void createRejectsDuplicateFarmerPhone() {
    when(farmerRepository.existsByPhone("9876543210")).thenReturn(true);
    FarmerService service =
        new FarmerService(
            farmerRepository,
            cacheService,
            storageService,
            afterCommitExecutor,
            transactionManager);

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
}
