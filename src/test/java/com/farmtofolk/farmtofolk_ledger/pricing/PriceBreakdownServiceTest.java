package com.farmtofolk.farmtofolk_ledger.pricing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PriceBreakdownServiceTest {

  @Mock private PriceBreakdownRepository priceBreakdownRepository;

  @Mock private BatchRepository batchRepository;

  @InjectMocks private PriceBreakdownService priceBreakdownService;

  @Test
  void createPriceBreakdownRejectsDuplicateForBatch() {
    UUID batchId = UUID.randomUUID();

    when(batchRepository.existsById(batchId)).thenReturn(true);
    when(priceBreakdownRepository.findByBatchId(batchId))
        .thenReturn(Optional.of(new PriceBreakdown()));

    CreatePriceBreakdownRequest request =
        new CreatePriceBreakdownRequest(null, null, null, "INR", "kg");

    assertThrows(
        ConflictException.class,
        () -> priceBreakdownService.createPriceBreakdown(batchId, request));
  }

  @Test
  void responseCalculatesMarginFromThreePriceFields() {
    PriceBreakdown priceBreakdown = new PriceBreakdown();
    priceBreakdown.setConsumerPrice(new BigDecimal("100"));
    priceBreakdown.setFarmerPrice(new BigDecimal("60"));
    priceBreakdown.setOperationalCost(new BigDecimal("20"));

    PriceBreakdownResponse response = PriceBreakdownResponse.from(priceBreakdown);

    assertEquals(new BigDecimal("20"), response.margin());
  }
}
