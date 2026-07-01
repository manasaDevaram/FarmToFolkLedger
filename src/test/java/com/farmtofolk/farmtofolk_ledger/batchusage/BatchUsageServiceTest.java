package com.farmtofolk.farmtofolk_ledger.batchusage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchUsageServiceTest {
  @Mock BatchUsageRepository usageRepository;
  @Mock BatchRepository batchRepository;
  @Mock PublicTraceCacheService cacheService;
  @Mock AfterCommitExecutor afterCommitExecutor;
  BatchUsageService service;

  @BeforeEach
  void setUp() {
    service = new BatchUsageService(usageRepository, batchRepository, cacheService, afterCommitExecutor);
  }

  @Test
  void soldUsageUpdatesCachedInventory() {
    UUID batchId = UUID.randomUUID();
    Batch batch = batchWithAvailable("100");
    when(batchRepository.findForUpdateById(batchId)).thenReturn(Optional.of(batch));
    when(usageRepository.save(any(BatchUsage.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.createUsage(batchId, new CreateBatchUsageRequest(
        BatchUsageType.SOLD_OFFLINE, new BigDecimal("25"), new BigDecimal("80"),
        null, null, null, null, null));

    assertEquals(new BigDecimal("25"), batch.getQuantitySold());
    assertEquals(new BigDecimal("75"), batch.getQuantityAvailable());
    verify(batchRepository).save(batch);
  }

  @Test
  void wasteDoesNotRequirePriceAndUpdatesWasteTotal() {
    UUID batchId = UUID.randomUUID();
    Batch batch = batchWithAvailable("10");
    when(batchRepository.findForUpdateById(batchId)).thenReturn(Optional.of(batch));
    when(usageRepository.save(any(BatchUsage.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.recordWaste(batchId, new CreateBatchWasteRequest(
        new BigDecimal("2"), "Spoilage", null, null));

    assertEquals(new BigDecimal("2"), batch.getQuantityWasted());
    assertEquals(new BigDecimal("8"), batch.getQuantityAvailable());
  }

  @Test
  void usageCannotExceedAvailableQuantity() {
    UUID batchId = UUID.randomUUID();
    when(batchRepository.findForUpdateById(batchId))
        .thenReturn(Optional.of(batchWithAvailable("5")));

    assertThrows(BadRequestException.class, () -> service.createUsage(batchId,
        new CreateBatchUsageRequest(BatchUsageType.USED_IN_PRODUCT, new BigDecimal("6"),
            null, null, null, null, null, null)));
  }

  @Test
  void soldUsageRequiresPricePerUnit() {
    UUID batchId = UUID.randomUUID();
    when(batchRepository.findForUpdateById(batchId))
        .thenReturn(Optional.of(batchWithAvailable("5")));

    assertThrows(BadRequestException.class, () -> service.createUsage(batchId,
        new CreateBatchUsageRequest(BatchUsageType.SOLD_ONLINE, new BigDecimal("1"),
            null, null, null, null, null, null)));
  }

  private Batch batchWithAvailable(String available) {
    Batch batch = new Batch();
    batch.setQuantityAvailable(new BigDecimal(available));
    batch.setQuantitySold(BigDecimal.ZERO);
    batch.setQuantityWasted(BigDecimal.ZERO);
    batch.setQuantityUsedInProduct(BigDecimal.ZERO);
    return batch;
  }
}
