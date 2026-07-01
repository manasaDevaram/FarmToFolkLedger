package com.farmtofolk.farmtofolk_ledger.batch;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

  @Mock private BatchRepository batchRepository;

  @Mock private FarmRepository farmRepository;

  @Mock private FarmerRepository farmerRepository;

  @Mock private PublicTraceCacheService publicTraceCacheService;

  @Mock private AfterCommitExecutor afterCommitExecutor;

  @InjectMocks private BatchService batchService;

  @Test
  void newBatchInitializesInventoryAndFarmerAmount() {
    Batch batch = new Batch();
    batch.setQuantityReceived(new BigDecimal("100"));
    batch.setFarmerPricePerUnit(new BigDecimal("60"));

    batch.initializeInventory();

    assertEquals(new BigDecimal("100"), batch.getQuantityAvailable());
    assertEquals(BigDecimal.ZERO, batch.getQuantitySold());
    assertEquals(BigDecimal.ZERO, batch.getQuantityWasted());
    assertEquals(BigDecimal.ZERO, batch.getQuantityUsedInProduct());
    assertEquals(new BigDecimal("6000"), batch.getTotalFarmerAmount());
  }

  @Test
  void createBatchRejectsFarmOwnedByDifferentFarmer() {
    UUID requestFarmerId = UUID.randomUUID();
    UUID actualFarmOwnerId = UUID.randomUUID();
    UUID farmId = UUID.randomUUID();
    Farm farm = new Farm();
    farm.setFarmerId(actualFarmOwnerId);

    when(farmerRepository.existsById(requestFarmerId)).thenReturn(true);
    when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

    CreateBatchRequest request =
        new CreateBatchRequest(
            "BATCH-001",
            farmId,
            requestFarmerId,
            "Tomato",
            "Local",
            BigDecimal.TEN,
            "kg",
            LocalDate.now(),
            LocalDate.now(),
            new BigDecimal("20"),
            com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus.UNPAID,
            new BigDecimal("40"),
            new BigDecimal("5"),
            "READY");

    assertThrows(BadRequestException.class, () -> batchService.createBatch(request));
  }

  @Test
  void updateBatchEvictsPublicTraceCache() {
    doAnswer(
            invocation -> {
              invocation.<Runnable>getArgument(0).run();
              return null;
            })
        .when(afterCommitExecutor)
        .run(org.mockito.ArgumentMatchers.any());
    UUID farmerId = UUID.randomUUID();
    UUID farmId = UUID.randomUUID();
    UUID batchId = UUID.randomUUID();
    Farm farm = new Farm();
    farm.setFarmerId(farmerId);
    Batch batch = new Batch();

    when(farmerRepository.existsById(farmerId)).thenReturn(true);
    when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(batchRepository.save(batch)).thenReturn(batch);

    CreateBatchRequest request =
        new CreateBatchRequest(
            "BATCH-002",
            farmId,
            farmerId,
            "Tomato",
            "Local",
            BigDecimal.TEN,
            "kg",
            LocalDate.now(),
            LocalDate.now(),
            new BigDecimal("20"),
            com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus.UNPAID,
            new BigDecimal("40"),
            new BigDecimal("5"),
            "READY");

    batchService.updateBatch(batchId, request);

    verify(publicTraceCacheService).evictStableDataForBatch(batchId);
  }

  @Test
  void createBatchRejectsDuplicateBatchCodeBeforeSaving() {
    UUID farmerId = UUID.randomUUID();
    UUID farmId = UUID.randomUUID();
    Farm farm = new Farm();
    farm.setFarmerId(farmerId);
    when(farmerRepository.existsById(farmerId)).thenReturn(true);
    when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));
    when(batchRepository.existsByBatchCode("BATCH-001")).thenReturn(true);

    CreateBatchRequest request =
        new CreateBatchRequest(
            "BATCH-001",
            farmId,
            farmerId,
            "Tomato",
            null,
            BigDecimal.TEN,
            "kg",
            LocalDate.now(),
            LocalDate.now(),
            new BigDecimal("20"),
            com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus.UNPAID,
            new BigDecimal("40"),
            new BigDecimal("5"),
            "READY");

    assertThrows(ConflictException.class, () -> batchService.createBatch(request));
  }
}
