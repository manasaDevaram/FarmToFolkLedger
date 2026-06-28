package com.farmtofolk.farmtofolk_ledger.adminpayments;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurement;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentServiceTest {

    @Mock BatchProcurementRepository procurementRepository;
    @Mock BatchRepository batchRepository;
    @Mock FarmerRepository farmerRepository;
    @Mock FarmRepository farmRepository;

    @Test
    void summaryGroupsPaymentsByFarmerAndStatus() {
        UUID farmerId = UUID.randomUUID();
        Fixture fixture = fixture(farmerId);
        stubFixture(fixture);

        FarmerPaymentSummaryResponse summary = service().getFarmerSummaries().getFirst();

        assertEquals(farmerId, summary.farmerId());
        assertEquals(new BigDecimal("3600"), summary.totalPayable());
        assertEquals(new BigDecimal("2400"), summary.totalPaid());
        assertEquals(new BigDecimal("1200"), summary.totalPending());
        assertEquals(1, summary.pendingBatchCount());
        assertEquals(2, summary.batches().size());
    }

    @Test
    void batchListSupportsCombinedFilters() {
        UUID farmerId = UUID.randomUUID();
        Fixture fixture = fixture(farmerId);
        stubFixture(fixture);

        List<AdminBatchPaymentResponse> results = service()
                .getBatchPayments("paid", farmerId, "tom");

        assertEquals(1, results.size());
        assertEquals("TOM-001", results.getFirst().batchCode());
    }

    @Test
    void batchListRejectsUnknownStatus() {
        assertThrows(BadRequestException.class, () -> service().getBatchPayments("PARTIAL", null, null));
    }

    @Test
    void paymentPatchOnlyChangesStatus() {
        UUID procurementId = UUID.randomUUID();
        BatchProcurement procurement = mock(BatchProcurement.class);
        when(procurementRepository.findById(procurementId)).thenReturn(Optional.of(procurement));
        when(procurementRepository.save(procurement)).thenReturn(procurement);

        service().updatePaymentStatus(procurementId, new UpdatePaymentStatusRequest("paid"));

        verify(procurement).setPaymentStatus("PAID");
        verify(procurementRepository).save(procurement);
    }

    private AdminPaymentService service() {
        return new AdminPaymentService(procurementRepository, batchRepository, farmerRepository, farmRepository);
    }

    private Fixture fixture(UUID farmerId) {
        UUID farmId = UUID.randomUUID();
        Batch firstBatch = batch(UUID.randomUUID(), farmerId, farmId, "TOM-001", "Tomato");
        Batch secondBatch = batch(UUID.randomUUID(), farmerId, farmId, "CHI-001", "Chilli");
        BatchProcurement paid = procurement(firstBatch.getId(), "60", "40", "PAID", 2);
        BatchProcurement unpaid = procurement(secondBatch.getId(), "30", "40", "UNPAID", 1);
        Farmer farmer = mock(Farmer.class);
        when(farmer.getId()).thenReturn(farmerId);
        when(farmer.getName()).thenReturn("Ramesh");
        when(farmer.getPhone()).thenReturn("9999999999");
        Farm farm = mock(Farm.class);
        when(farm.getId()).thenReturn(farmId);
        when(farm.getFarmName()).thenReturn("Green Farm");
        return new Fixture(List.of(paid, unpaid), List.of(firstBatch, secondBatch), farmer, farm);
    }

    private void stubFixture(Fixture fixture) {
        when(procurementRepository.findAll()).thenReturn(fixture.procurements());
        when(batchRepository.findAllById(any())).thenReturn(fixture.batches());
        when(farmerRepository.findAllById(any())).thenReturn(List.of(fixture.farmer()));
        when(farmRepository.findAllById(any())).thenReturn(List.of(fixture.farm()));
    }

    private Batch batch(UUID id, UUID farmerId, UUID farmId, String code, String crop) {
        Batch batch = mock(Batch.class);
        when(batch.getId()).thenReturn(id);
        when(batch.getFarmerId()).thenReturn(farmerId);
        when(batch.getFarmId()).thenReturn(farmId);
        when(batch.getBatchCode()).thenReturn(code);
        when(batch.getCropName()).thenReturn(crop);
        when(batch.getUnit()).thenReturn("kg");
        return batch;
    }

    private BatchProcurement procurement(
            UUID batchId,
            String quantity,
            String price,
            String status,
            int daysAgo
    ) {
        BatchProcurement procurement = mock(BatchProcurement.class);
        BigDecimal quantityValue = new BigDecimal(quantity);
        BigDecimal priceValue = new BigDecimal(price);
        when(procurement.getId()).thenReturn(UUID.randomUUID());
        when(procurement.getBatchId()).thenReturn(batchId);
        when(procurement.getQuantityTaken()).thenReturn(quantityValue);
        when(procurement.getFarmerPricePerUnit()).thenReturn(priceValue);
        when(procurement.getFarmerAmountPayable()).thenReturn(quantityValue.multiply(priceValue));
        when(procurement.getPaymentStatus()).thenReturn(status);
        when(procurement.getCurrency()).thenReturn("INR");
        when(procurement.getProcuredAt()).thenReturn(LocalDateTime.now().minusDays(daysAgo));
        return procurement;
    }

    private record Fixture(
            List<BatchProcurement> procurements,
            List<Batch> batches,
            Farmer farmer,
            Farm farm
    ) {
    }
}
