package com.farmtofolk.farmtofolk_ledger.adminoverview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.batch.*;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaRepository;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.*;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransactionRepository;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsageRepository;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import com.farmtofolk.farmtofolk_ledger.verification.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminOverviewServiceTest {
  @Test
  void dashboardGroupsPriorityMetricsFromBatchesAndFarmVerifications() {
    FarmerRepository farmers = mock(FarmerRepository.class);
    FarmRepository farms = mock(FarmRepository.class);
    BatchRepository batches = mock(BatchRepository.class);
    FarmVerificationRepository verifications = mock(FarmVerificationRepository.class);
    Batch pending = batch("100", "100", "30", "5", PaymentStatus.UNPAID, "60");
    Batch paid = batch("50", "10", "10", "0", PaymentStatus.PAID, "40");
    FarmVerification pendingVerification = new FarmVerification();
    pendingVerification.setStatus("PENDING");
    FarmVerification upcoming = new FarmVerification();
    upcoming.setStatus("COMPLETED");
    upcoming.setNextVerificationDue(LocalDate.now().plusDays(7));
    when(batches.findAll()).thenReturn(List.of(pending, paid));
    when(verifications.findAll()).thenReturn(List.of(pendingVerification, upcoming));
    when(farmers.count()).thenReturn(3L);
    when(farms.count()).thenReturn(4L);
    when(batches.count()).thenReturn(2L);

    AdminOverviewService service = new AdminOverviewService(
        farmers, farms, batches, mock(BatchUsageRepository.class), mock(FarmMediaRepository.class),
        verifications, mock(VerificationEvidenceRepository.class),
        mock(TraceEventRepository.class),
        mock(QrCodeRepository.class), mock(StorageService.class));

    AdminDashboardResponse response = service.getDashboard();

    assertEquals(new BigDecimal("6000"), response.payments().pendingAmount());
    assertEquals(1, response.payments().pendingCount());
    assertEquals(1, response.verifications().pendingCount());
    assertEquals(1, response.verifications().upcomingCount());
    assertEquals(new BigDecimal("110"), response.inventory().totalAvailableQuantity());
    assertEquals(new BigDecimal("40"), response.inventory().totalSoldQuantity());
    assertEquals(new BigDecimal("5"), response.inventory().totalWastedQuantity());
  }

  private Batch batch(
      String received, String available, String sold, String wasted,
      PaymentStatus paymentStatus, String farmerPrice) {
    Batch batch = new Batch();
    batch.setQuantityAvailable(new BigDecimal(available));
    batch.setQuantitySold(new BigDecimal(sold));
    batch.setQuantityReceived(new BigDecimal(received));
    batch.setQuantityWasted(new BigDecimal(wasted));
    batch.setPaymentStatus(paymentStatus);
    batch.setFarmerPricePerUnit(new BigDecimal(farmerPrice));
    batch.calculateTotalFarmerAmount();
    return batch;
  }
}
