package com.farmtofolk.farmtofolk_ledger.batch;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private FarmerRepository farmerRepository;

    @Mock
    private PublicTraceCacheService publicTraceCacheService;

    @InjectMocks
    private BatchService batchService;

    @Test
    void createBatchRejectsFarmOwnedByDifferentFarmer() {
        UUID requestFarmerId = UUID.randomUUID();
        UUID actualFarmOwnerId = UUID.randomUUID();
        UUID farmId = UUID.randomUUID();
        Farm farm = new Farm();
        farm.setFarmerId(actualFarmOwnerId);

        when(farmerRepository.existsById(requestFarmerId)).thenReturn(true);
        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

        CreateBatchRequest request = new CreateBatchRequest(
                "BATCH-001",
                farmId,
                requestFarmerId,
                "Tomato",
                "Local",
                BigDecimal.TEN,
                "kg",
                LocalDate.now(),
                null,
                null,
                "READY"
        );

        assertThrows(BadRequestException.class, () -> batchService.createBatch(request));
    }

    @Test
    void updateBatchEvictsPublicTraceCache() {
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

        CreateBatchRequest request = new CreateBatchRequest(
                "BATCH-002",
                farmId,
                farmerId,
                "Tomato",
                "Local",
                BigDecimal.TEN,
                "kg",
                LocalDate.now(),
                null,
                null,
                "READY"
        );

        batchService.updateBatch(batchId, request);

        verify(publicTraceCacheService).evictStableDataForBatch(batchId);
    }
}
