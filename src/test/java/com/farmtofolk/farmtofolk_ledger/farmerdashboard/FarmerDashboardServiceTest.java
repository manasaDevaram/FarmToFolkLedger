package com.farmtofolk.farmtofolk_ledger.farmerdashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.auth.CurrentUserService;
import com.farmtofolk.farmtofolk_ledger.auth.User;
import com.farmtofolk.farmtofolk_ledger.auth.UserRepository;
import com.farmtofolk.farmtofolk_ledger.auth.UserRole;
import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurement;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransaction;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransactionRepository;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class FarmerDashboardServiceTest {

  @Mock CurrentUserService currentUserService;
  @Mock UserRepository userRepository;
  @Mock FarmerRepository farmerRepository;
  @Mock FarmRepository farmRepository;
  @Mock BatchRepository batchRepository;
  @Mock TraceEventRepository traceEventRepository;
  @Mock PriceBreakdownRepository priceBreakdownRepository;
  @Mock BatchProcurementRepository procurementRepository;
  @Mock BatchSaleTransactionRepository saleTransactionRepository;

  @Test
  void batchSummaryAggregatesProcurementAndAllSaleTransactions() {
    UUID farmerId = UUID.randomUUID();
    UUID farmId = UUID.randomUUID();
    UUID batchId = UUID.randomUUID();
    User admin = mock(User.class);
    Farmer farmer = mock(Farmer.class);
    Farm farm = mock(Farm.class);
    Batch batch = mock(Batch.class);
    when(admin.getRole()).thenReturn(UserRole.ADMIN);
    when(currentUserService.getCurrentUser()).thenReturn(admin);
    when(farmerRepository.findById(farmerId)).thenReturn(Optional.of(farmer));
    when(farmer.getId()).thenReturn(farmerId);
    when(batchRepository.findByFarmerId(farmerId)).thenReturn(List.of(batch));
    when(batch.getId()).thenReturn(batchId);
    when(batch.getFarmId()).thenReturn(farmId);
    when(batch.getCropName()).thenReturn("Tomato");
    when(batch.getStatus()).thenReturn("ACTIVE");
    when(batch.getUnit()).thenReturn("kg");
    when(farmRepository.findAllById(org.mockito.ArgumentMatchers.<Iterable<UUID>>any()))
        .thenReturn(List.of(farm));
    when(farm.getId()).thenReturn(farmId);
    when(traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId)).thenReturn(List.of());
    when(procurementRepository.findByBatchId(batchId))
        .thenReturn(Optional.of(procurement(batchId, "100", "40")));
    when(saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId))
        .thenReturn(List.of(sale(batchId, "25", "70"), sale(batchId, "15", "80")));

    FarmerDashboardBatchResponse response = service().getBatches(farmerId).getFirst();

    assertEquals(new BigDecimal("100"), response.quantityTaken());
    assertEquals(new BigDecimal("40"), response.totalQuantitySold());
    assertEquals(new BigDecimal("60"), response.quantityRemaining());
    assertEquals(new BigDecimal("2950"), response.totalSaleAmount());
    assertEquals(new BigDecimal("4000"), response.farmerAmountPayable());
    assertEquals(PaymentStatus.UNPAID, response.paymentStatus());
  }

  @Test
  void farmerCannotReadAnotherFarmersBatch() {
    UUID userId = UUID.randomUUID();
    UUID ownFarmerId = UUID.randomUUID();
    UUID otherFarmerId = UUID.randomUUID();
    UUID batchId = UUID.randomUUID();
    User user = mock(User.class);
    Farmer farmer = mock(Farmer.class);
    Batch batch = mock(Batch.class);
    when(user.getId()).thenReturn(userId);
    when(user.getRole()).thenReturn(UserRole.FARMER);
    when(currentUserService.getCurrentUser()).thenReturn(user);
    when(farmerRepository.findByUserId(userId)).thenReturn(Optional.of(farmer));
    when(farmer.getId()).thenReturn(ownFarmerId);
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(batch.getFarmerId()).thenReturn(otherFarmerId);

    assertThrows(AccessDeniedException.class, () -> service().getBatchDetail(batchId));
  }

  private FarmerDashboardService service() {
    return new FarmerDashboardService(
        currentUserService,
        userRepository,
        farmerRepository,
        farmRepository,
        batchRepository,
        traceEventRepository,
        priceBreakdownRepository,
        procurementRepository,
        saleTransactionRepository);
  }

  private BatchProcurement procurement(UUID batchId, String quantity, String price) {
    BatchProcurement procurement = new BatchProcurement();
    procurement.setBatchId(batchId);
    procurement.setQuantityTaken(new BigDecimal(quantity));
    procurement.setFarmerPricePerUnit(new BigDecimal(price));
    procurement.setPaymentStatus(PaymentStatus.UNPAID);
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
