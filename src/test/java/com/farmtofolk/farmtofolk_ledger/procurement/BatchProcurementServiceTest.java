package com.farmtofolk.farmtofolk_ledger.procurement;

import com.farmtofolk.farmtofolk_ledger.auth.CurrentUserService;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ConflictException;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransaction;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransactionRepository;
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
class BatchProcurementServiceTest {

    @Mock BatchProcurementRepository procurementRepository;
    @Mock BatchRepository batchRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock CurrentUserService currentUserService;
    @Mock BatchSaleTransactionRepository saleTransactionRepository;

    @Test
    void createCalculatesPayableAndDefaultsCurrency() {
        UUID batchId = UUID.randomUUID();
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(new Batch()));
        when(procurementRepository.existsByBatchId(batchId)).thenReturn(false);
        when(procurementRepository.save(any(BatchProcurement.class))).thenAnswer(invocation -> {
            BatchProcurement procurement = invocation.getArgument(0);
            procurement.prePersist();
            return procurement;
        });

        BatchProcurementResponse response = service().create(batchId, new CreateBatchProcurementRequest(
                new BigDecimal("120.5"), new BigDecimal("42.25"), "unpaid", null, null
        ));

        assertEquals(new BigDecimal("5091.125"), response.farmerAmountPayable());
        assertEquals("UNPAID", response.paymentStatus());
        assertEquals("INR", response.currency());
    }

    @Test
    void createRejectsDuplicateProcurement() {
        UUID batchId = UUID.randomUUID();
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(new Batch()));
        when(procurementRepository.existsByBatchId(batchId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service().create(batchId,
                new CreateBatchProcurementRequest(BigDecimal.ONE, BigDecimal.ONE, "PAID", "INR", null)));
    }

    @Test
    void updateCannotReduceQuantityBelowExistingSales() {
        UUID batchId = UUID.randomUUID();
        BatchProcurement procurement = procurement(batchId, "20");
        BatchSaleTransaction sale = sale(batchId, "12", "50");
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(new Batch()));
        when(procurementRepository.findByBatchIdForUpdate(batchId)).thenReturn(Optional.of(procurement));
        when(saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId)).thenReturn(List.of(sale));

        assertThrows(BadRequestException.class, () -> service().update(batchId,
                new CreateBatchProcurementRequest(new BigDecimal("10"), BigDecimal.ONE, "PAID", "INR", null)));
    }

    private BatchProcurementService service() {
        return new BatchProcurementService(
                procurementRepository, batchRepository, farmerRepository, currentUserService,
                saleTransactionRepository
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
