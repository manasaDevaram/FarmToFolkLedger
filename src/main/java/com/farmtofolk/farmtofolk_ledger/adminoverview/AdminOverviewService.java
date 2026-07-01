package com.farmtofolk.farmtofolk_ledger.adminoverview;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaRepository;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaResponse;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownRepository;
import com.farmtofolk.farmtofolk_ledger.pricing.PriceBreakdownResponse;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurement;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementRepository;
import com.farmtofolk.farmtofolk_ledger.procurement.BatchProcurementResponse;
import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeResponse;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransaction;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransactionRepository;
import com.farmtofolk.farmtofolk_ledger.sales.BatchSaleTransactionResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerification;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationRepository;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationResponse;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceRepository;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceResponse;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import java.math.BigDecimal;
import java.util.List;
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
public class AdminOverviewService {

    private final FarmerRepository farmerRepository;
    private final FarmRepository farmRepository;
    private final BatchRepository batchRepository;
    private final BatchProcurementRepository procurementRepository;
    private final BatchSaleTransactionRepository saleTransactionRepository;
    private final FarmMediaRepository farmMediaRepository;
    private final FarmVerificationRepository farmVerificationRepository;
    private final VerificationEvidenceRepository verificationEvidenceRepository;
    private final PriceBreakdownRepository priceBreakdownRepository;
    private final TraceEventRepository traceEventRepository;
    private final QrCodeRepository qrCodeRepository;
    private final StorageService storageService;

    public AdminOverviewService(
            FarmerRepository farmerRepository,
            FarmRepository farmRepository,
            BatchRepository batchRepository,
            BatchProcurementRepository procurementRepository,
            BatchSaleTransactionRepository saleTransactionRepository,
            FarmMediaRepository farmMediaRepository,
            FarmVerificationRepository farmVerificationRepository,
            VerificationEvidenceRepository verificationEvidenceRepository,
            PriceBreakdownRepository priceBreakdownRepository,
            TraceEventRepository traceEventRepository,
            QrCodeRepository qrCodeRepository,
            StorageService storageService) {
        this.farmerRepository = farmerRepository;
        this.farmRepository = farmRepository;
        this.batchRepository = batchRepository;
        this.procurementRepository = procurementRepository;
        this.saleTransactionRepository = saleTransactionRepository;
        this.farmMediaRepository = farmMediaRepository;
        this.farmVerificationRepository = farmVerificationRepository;
        this.verificationEvidenceRepository = verificationEvidenceRepository;
        this.priceBreakdownRepository = priceBreakdownRepository;
        this.traceEventRepository = traceEventRepository;
        this.qrCodeRepository = qrCodeRepository;
        this.storageService = storageService;
    }

    public AdminDashboardSummaryResponse getDashboardSummary() {
        long totalFarmers = farmerRepository.count();
        long activeFarmers = farmerRepository.findAll().stream().filter(f -> Boolean.TRUE.equals(f.getActive()))
                .count();
        long totalFarms = farmRepository.count();
        long totalBatches = batchRepository.count();
        long totalQrCodes = qrCodeRepository.count();

        // Pending payments: aggregate over procurements with UNPAID status.
        List<BatchProcurement> allProcurements = procurementRepository.findAll();
        BigDecimal pendingPaymentsAmount = allProcurements.stream()
                .filter(p -> PaymentStatus.UNPAID == p.getPaymentStatus())
                .map(p -> p.getFarmerAmountPayable() == null ? BigDecimal.ZERO : p.getFarmerAmountPayable())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingPaymentBatchCount = allProcurements.stream()
                .filter(p -> PaymentStatus.UNPAID == p.getPaymentStatus())
                .count();

        // Recent verifications: load last 5 across all farms.
        List<FarmVerificationResponse> recentVerifications = farmVerificationRepository.findAll().stream()
                .sorted(
                        (a, b) -> {
                            if (a.getVerificationDate() == null)
                                return 1;
                            if (b.getVerificationDate() == null)
                                return -1;
                            return b.getVerificationDate().compareTo(a.getVerificationDate());
                        })
                .limit(5)
                .map(FarmVerificationResponse::from)
                .toList();

        return new AdminDashboardSummaryResponse(
                totalFarmers,
                activeFarmers,
                totalFarms,
                totalBatches,
                pendingPaymentsAmount,
                pendingPaymentBatchCount,
                recentVerifications,
                totalQrCodes);
    }

    public AdminFarmerOverviewResponse getFarmerOverview(UUID farmerId) {
        Farmer farmer = farmerRepository
                .findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        List<Farm> farms = farmRepository.findByFarmerId(farmerId);
        List<Batch> batches = batchRepository.findByFarmerId(farmerId);

        // One procurement per batch – load all in one query and index by batchId.
        List<UUID> batchIds = batches.stream().map(Batch::getId).toList();
        Map<UUID, BatchProcurement> procurementsByBatchId = procurementRepository.findAll().stream()
                .filter(p -> batchIds.contains(p.getBatchId()))
                .collect(Collectors.toMap(BatchProcurement::getBatchId, Function.identity()));

        BigDecimal totalPayable = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;

        for (BatchProcurement p : procurementsByBatchId.values()) {
            BigDecimal amount = p.getFarmerAmountPayable() == null ? BigDecimal.ZERO : p.getFarmerAmountPayable();
            totalPayable = totalPayable.add(amount);
            if (PaymentStatus.PAID == p.getPaymentStatus()) {
                totalPaid = totalPaid.add(amount);
            } else {
                totalPending = totalPending.add(amount);
            }
        }

        return new AdminFarmerOverviewResponse(
                FarmerResponse.from(farmer, storageService),
                farms.stream().map(FarmResponse::from).toList(),
                batches.stream().map(BatchResponse::from).toList(),
                new AdminFarmerPaymentSummary(totalPayable, totalPaid, totalPending));
    }

    public AdminFarmOverviewResponse getFarmOverview(UUID farmId) {
        Farm farm = farmRepository
                .findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found"));

        Farmer farmer = farm.getFarmerId() == null
                ? null
                : farmerRepository.findById(farm.getFarmerId()).orElse(null);

        List<FarmMediaResponse> media = farmMediaRepository.findByFarmIdOrderByCreatedAtAsc(farmId).stream()
                .map(mediaItem -> FarmMediaResponse.from(mediaItem, storageService))
                .toList();

        FarmVerification latestVerification = farmVerificationRepository
                .findFirstByFarmIdOrderByVerificationDateDesc(farmId)
                .orElse(null);

        List<VerificationEvidenceResponse> verificationEvidence = latestVerification == null
                ? List.of()
                : verificationEvidenceRepository
                        .findByVerificationIdOrderByCreatedAtAsc(latestVerification.getId())
                        .stream()
                        .map(evidence -> VerificationEvidenceResponse.from(evidence, storageService))
                        .toList();

        List<BatchResponse> batches = batchRepository.findByFarmId(farmId).stream().map(BatchResponse::from).toList();

        return new AdminFarmOverviewResponse(
                FarmResponse.from(farm),
                farmer == null ? null : FarmerResponse.from(farmer, storageService),
                media,
                latestVerification == null ? null : FarmVerificationResponse.from(latestVerification),
                verificationEvidence,
                batches);
    }

    public AdminBatchOverviewResponse getBatchOverview(UUID batchId) {
        Batch batch = batchRepository
                .findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found"));

        Farmer farmer = batch.getFarmerId() == null
                ? null
                : farmerRepository.findById(batch.getFarmerId()).orElse(null);

        Farm farm = batch.getFarmId() == null
                ? null
                : farmRepository.findById(batch.getFarmId()).orElse(null);

        BatchProcurementResponse procurement = procurementRepository
                .findByBatchId(batchId)
                .map(BatchProcurementResponse::from)
                .orElse(null);

        List<BatchSaleTransaction> saleTxs = saleTransactionRepository.findByBatchIdOrderBySoldAtAsc(batchId);
        List<BatchSaleTransactionResponse> saleTransactions = saleTxs.stream().map(BatchSaleTransactionResponse::from)
                .toList();

        BigDecimal totalSold = saleTxs.stream()
                .map(t -> t.getQuantitySold() == null ? BigDecimal.ZERO : t.getQuantitySold())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal quantityTaken = procurement == null || procurement.quantityTaken() == null
                ? BigDecimal.ZERO
                : procurement.quantityTaken();
        BigDecimal remaining = quantityTaken.subtract(totalSold);
        BigDecimal totalSaleAmount = saleTxs.stream()
                .map(t -> t.getSaleAmount() == null ? BigDecimal.ZERO : t.getSaleAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        AdminBatchSalesSummary salesSummary = new AdminBatchSalesSummary(totalSold, remaining, totalSaleAmount);

        PriceBreakdownResponse priceBreakdown = priceBreakdownRepository
                .findByBatchId(batchId)
                .map(PriceBreakdownResponse::from)
                .orElse(null);

        List<TraceEventResponse> traceEvents = traceEventRepository.findByBatchIdOrderByEventTimeAsc(batchId).stream()
                .map(TraceEventResponse::from)
                .toList();

        QrCodeResponse qrCode = qrCodeRepository
                .findFirstByBatchIdAndIsActiveTrue(batchId)
                .map(QrCodeResponse::from)
                .orElse(null);

        return new AdminBatchOverviewResponse(
                BatchResponse.from(batch),
                farmer == null ? null : FarmerResponse.from(farmer, storageService),
                farm == null ? null : FarmResponse.from(farm),
                procurement,
                saleTransactions,
                salesSummary,
                priceBreakdown,
                traceEvents,
                qrCode);
    }
}
