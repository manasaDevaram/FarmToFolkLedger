package com.farmtofolk.farmtofolk_ledger.adminpayments;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurement;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementResponse;
import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentService {

  private final BatchProcurementRepository procurementRepository;
  private final BatchRepository batchRepository;
  private final FarmerRepository farmerRepository;
  private final FarmRepository farmRepository;

  public AdminPaymentService(
      BatchProcurementRepository procurementRepository,
      BatchRepository batchRepository,
      FarmerRepository farmerRepository,
      FarmRepository farmRepository) {
    this.procurementRepository = procurementRepository;
    this.batchRepository = batchRepository;
    this.farmerRepository = farmerRepository;
    this.farmRepository = farmRepository;
  }

  public List<FarmerPaymentSummaryResponse> getFarmerSummaries() {
    Map<UUID, List<AdminBatchPaymentResponse>> byFarmer =
        loadPayments().stream()
            .collect(
                Collectors.groupingBy(
                    AdminBatchPaymentResponse::farmerId, LinkedHashMap::new, Collectors.toList()));

    return byFarmer.values().stream()
        .map(
            payments -> {
              AdminBatchPaymentResponse first = payments.getFirst();
              BigDecimal totalPaid = totalForStatus(payments, PaymentStatus.PAID);
              BigDecimal totalPending = totalForStatus(payments, PaymentStatus.UNPAID);
              List<FarmerPaymentBatchResponse> batches =
                  payments.stream().map(this::toFarmerBatch).toList();
              return new FarmerPaymentSummaryResponse(
                  first.farmerId(),
                  first.farmerName(),
                  first.farmerPhone(),
                  totalPaid.add(totalPending),
                  totalPaid,
                  totalPending,
                  payments.stream()
                      .filter(payment -> payment.paymentStatus() == PaymentStatus.UNPAID)
                      .count(),
                  batches);
            })
        .sorted(
            Comparator.comparing(
                FarmerPaymentSummaryResponse::farmerName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
        .toList();
  }

  public List<AdminBatchPaymentResponse> getBatchPayments(
      String status, UUID farmerId, String cropName) {
    PaymentStatus normalizedStatus = normalizeStatus(status);
    String normalizedCrop = cropName == null ? null : cropName.trim().toLowerCase(Locale.ROOT);
    return loadPayments().stream()
        .filter(
            payment -> normalizedStatus == null || normalizedStatus.equals(payment.paymentStatus()))
        .filter(payment -> farmerId == null || farmerId.equals(payment.farmerId()))
        .filter(
            payment ->
                normalizedCrop == null
                    || normalizedCrop.isBlank()
                    || (payment.cropName() != null
                        && payment.cropName().toLowerCase(Locale.ROOT).contains(normalizedCrop)))
        .toList();
  }

  @Transactional
  public BatchProcurementResponse updatePaymentStatus(
      UUID procurementId, UpdatePaymentStatusRequest request) {
    BatchProcurement procurement =
        procurementRepository
            .findById(procurementId)
            .orElseThrow(() -> new ResourceNotFoundException("Procurement not found"));
    procurement.setPaymentStatus(request.paymentStatus());
    return BatchProcurementResponse.from(procurementRepository.save(procurement));
  }

  @Transactional
  public BatchResponse updateBatchPaymentStatus(UUID batchId, UpdatePaymentStatusRequest request) {
    Batch batch = batchRepository.findById(batchId)
        .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));
    batch.setPaymentStatus(request.paymentStatus());
    return BatchResponse.from(batchRepository.save(batch));
  }

  private List<AdminBatchPaymentResponse> loadPayments() {
    List<Batch> batchList = batchRepository.findAll();
    Map<UUID, Batch> batches = batchList.stream()
        .collect(Collectors.toMap(Batch::getId, Function.identity()));
    Map<UUID, Farmer> farmers =
        farmerRepository
            .findAllById(
                batches.values().stream().map(Batch::getFarmerId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Farmer::getId, Function.identity()));
    Map<UUID, Farm> farms =
        farmRepository
            .findAllById(
                batches.values().stream().map(Batch::getFarmId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Farm::getId, Function.identity()));

    List<AdminBatchPaymentResponse> payments = new ArrayList<>();
    for (Batch batch : batchList) {
      Farmer farmer = farmers.get(batch.getFarmerId());
      Farm farm = farms.get(batch.getFarmId());
      payments.add(
          new AdminBatchPaymentResponse(
              batch.getId(),
              batch.getFarmerId(),
              farmer == null ? null : farmer.getName(),
              farmer == null ? null : farmer.getPhone(),
              batch.getFarmId(),
              farm == null ? null : farm.getFarmName(),
              batch.getId(),
              batch.getBatchCode(),
              batch.getCropName(),
              batch.getQuantityReceived(),
              batch.getUnit(),
              batch.getFarmerPricePerUnit(),
              batch.getTotalFarmerAmount(),
              batch.getPaymentStatus(),
              "INR",
              batch.getReceivedDate() == null ? null : batch.getReceivedDate().atStartOfDay()));
    }
    return payments.stream()
        .sorted(
            Comparator.comparing(
                AdminBatchPaymentResponse::procuredAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
        .toList();
  }

  private BigDecimal totalForStatus(
      List<AdminBatchPaymentResponse> payments, PaymentStatus status) {
    return payments.stream()
        .filter(payment -> status == payment.paymentStatus())
        .map(AdminBatchPaymentResponse::farmerAmountPayable)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private FarmerPaymentBatchResponse toFarmerBatch(AdminBatchPaymentResponse payment) {
    return new FarmerPaymentBatchResponse(
        payment.batchId(),
        payment.batchCode(),
        payment.cropName(),
        payment.quantityTaken(),
        payment.unit(),
        payment.farmerPricePerUnit(),
        payment.farmerAmountPayable(),
        payment.paymentStatus(),
        payment.procuredAt());
  }

  private PaymentStatus normalizeStatus(String status) {
    if (status == null || status.isBlank()) return null;
    try {
      return PaymentStatus.from(status);
    } catch (IllegalArgumentException exception) {
      throw new BadRequestException("status must be PAID or UNPAID");
    }
  }
}
