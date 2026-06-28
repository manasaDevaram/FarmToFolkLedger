package com.farmtofolk.farmtofolk_ledger.sales;

import com.farmtofolk.farmtofolk_ledger.auth.CurrentUserService;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurement;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchSaleTransactionServiceTest {

    @Mock BatchSaleTransactionRepository saleTransactionRepository;
    @Mock BatchProcurementRepository procurementRepository;
    @Mock BatchRepository batchRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock CurrentUserService currentUserService;

    @Test
    void createAppendsTransactionAndCalculatesSaleAmount() {
        UUID batchId = UUID.randomUUID();
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(new Batch()));
        when(procurementRepository.findByBatchIdForUpdate(batchId))
                .thenReturn(Optional.of(procurement(batchId, "100")));
        when(saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId)).thenReturn(List.of());
        when(saleTransactionRepository.save(any(BatchSaleTransaction.class))).thenAnswer(invocation -> {
            BatchSaleTransaction sale = invocation.getArgument(0);
            sale.prePersist();
            return sale;
        });

        BatchSaleTransactionResponse response = service().create(batchId,
                new CreateBatchSaleTransactionRequest(
                        new BigDecimal("12.5"), new BigDecimal("80"), null, null
                ));

        assertEquals(new BigDecimal("1000.0"), response.saleAmount());
        assertEquals("INR", response.currency());
    }

    @Test
    void createRequiresProcurementFirst() {
        UUID batchId = UUID.randomUUID();
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(new Batch()));
        when(procurementRepository.findByBatchIdForUpdate(batchId)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> service().create(batchId,
                new CreateBatchSaleTransactionRequest(BigDecimal.ONE, BigDecimal.TEN, "INR", null)));
        assertEquals("Procurement must be recorded before sale transactions", exception.getMessage());
    }

    @Test
    void createRejectsCumulativeOverselling() {
        UUID batchId = UUID.randomUUID();
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(new Batch()));
        when(procurementRepository.findByBatchIdForUpdate(batchId))
                .thenReturn(Optional.of(procurement(batchId, "20")));
        when(saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId))
                .thenReturn(List.of(sale(batchId, "15", "60")));

        assertThrows(BadRequestException.class, () -> service().create(batchId,
                new CreateBatchSaleTransactionRequest(new BigDecimal("6"), BigDecimal.TEN, "INR", null)));
    }

    private BatchSaleTransactionService service() {
        return new BatchSaleTransactionService(
                saleTransactionRepository, procurementRepository, batchRepository, farmerRepository,
                currentUserService
        );
    }

    private BatchProcurement procurement(UUID batchId, String quantity) {
        BatchProcurement procurement = new BatchProcurement();
        procurement.setBatchId(batchId);
        procurement.setQuantityTaken(new BigDecimal(quantity));
        procurement.setFarmerPricePerUnit(BigDecimal.ONE);
        procurement.setPaymentStatus("UNPAID");
        procurement.calculateFarmerAmountPayable();
        return procurement;
    }

    private BatchSaleTransaction sale(UUID batchId, String quantity, String price) {
        BatchSaleTransaction sale = new BatchSaleTransaction();
        sale.setBatchId(batchId);
        sale.setQuantitySold(new BigDecimal(quantity));
        sale.setSalePricePerUnit(new BigDecimal(price));
        sale.calculateSaleAmount();
        return sale;
    }
}
