package com.farmtofolk.farmtofolk_ledger.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PriceBreakdownServiceTest {

  @Mock private BatchRepository batchRepository;
  @Mock private PublicTraceCacheService publicTraceCacheService;
  @Mock private AfterCommitExecutor afterCommitExecutor;

  @InjectMocks private PriceBreakdownService priceBreakdownService;

  @Test
  void createPriceBreakdownRejectsDuplicateForBatch() {
    UUID batchId = UUID.randomUUID();
    Batch batch = new Batch();
    batch.setConsumerPricePerUnit(new BigDecimal("100"));

    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

    CreatePriceBreakdownRequest request =
        new CreatePriceBreakdownRequest(
            new BigDecimal("100"),
            new BigDecimal("60"),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            new BigDecimal("20"),
            "INR",
            "kg");

    assertThrows(
        ConflictException.class,
        () -> priceBreakdownService.createPriceBreakdown(batchId, request));
  }

  @Test
  void updatePriceBreakdownPersistsOnBatch() {
    UUID batchId = UUID.randomUUID();
    Batch batch = new Batch();
    ReflectionTestUtils.setField(batch, "id", batchId);

    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(batchRepository.save(any(Batch.class))).thenAnswer(invocation -> invocation.getArgument(0));

    CreatePriceBreakdownRequest request =
        new CreatePriceBreakdownRequest(
            new BigDecimal("100"),
            new BigDecimal("60"),
            new BigDecimal("5"),
            new BigDecimal("3"),
            new BigDecimal("20"),
            "INR",
            "kg");

    PriceBreakdownResponse response =
        priceBreakdownService.updatePriceBreakdown(batchId, request);

    assertEquals(new BigDecimal("100"), response.consumerPrice());
    assertEquals(new BigDecimal("12"), response.margin());
    verify(batchRepository).save(batch);
  }
}
