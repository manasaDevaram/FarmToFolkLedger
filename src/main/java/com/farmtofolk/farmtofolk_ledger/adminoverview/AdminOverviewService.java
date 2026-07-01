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
import com.farmtofolk.farmtofolk_ledger.procurement.PaymentStatus;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeResponse;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventRepository;
import com.farmtofolk.farmtofolk_ledger.traceability.TraceEventResponse;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerification;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationRepository;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationResponse;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceRepository;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceResponse;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsage;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsageRepository;
import com.farmtofolk.farmtofolk_ledger.batchusage.BatchUsageResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.time.LocalDate;
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
    private final BatchUsageRepository batchUsageRepository;
    private final FarmMediaRepository farmMediaRepository;
    private final FarmVerificationRepository farmVerificationRepository;
    private final VerificationEvidenceRepository verificationEvidenceRepository;
    private final TraceEventRepository traceEventRepository;
    private final QrCodeRepository qrCodeRepository;
    private final StorageService storageService;

    public AdminOverviewService(
            FarmerRepository farmerRepository,
            FarmRepository farmRepository,
            BatchRepository batchRepository,
            BatchUsageRepository batchUsageRepository,
            FarmMediaRepository farmMediaRepository,
            FarmVerificationRepository farmVerificationRepository,
            VerificationEvidenceRepository verificationEvidenceRepository,
            TraceEventRepository traceEventRepository,
            QrCodeRepository qrCodeRepository,
            StorageService storageService) {
        this.farmerRepository = farmerRepository;
        this.farmRepository = farmRepository;
        this.batchRepository = batchRepository;
        this.batchUsageRepository = batchUsageRepository;
        this.farmMediaRepository = farmMediaRepository;
        this.farmVerificationRepository = farmVerificationRepository;
        this.verificationEvidenceRepository = verificationEvidenceRepository;
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

        // Compatibility summary now reads the central Batch payment snapshot.
        List<Batch> allBatches = batchRepository.findAll();
        BigDecimal pendingPaymentsAmount = allBatches.stream()
                .filter(batch -> PaymentStatus.UNPAID == batch.getPaymentStatus())
                .map(batch -> zero(batch.getTotalFarmerAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingPaymentBatchCount = allBatches.stream()
                .filter(batch -> PaymentStatus.UNPAID == batch.getPaymentStatus())
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

    public AdminDashboardResponse getDashboard() {
        List<Batch> batches = batchRepository.findAll();
        List<FarmVerification> verifications = farmVerificationRepository.findAll();
        BigDecimal pendingAmount = getPendingPaymentBatches(batches).stream()
                .map(batch -> zero(batch.getTotalFarmerAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingVerifications = verifications.stream().filter(this::isPendingVerification).count();
        long upcomingVerifications = verifications.stream().filter(this::isUpcomingVerification).count();
        return new AdminDashboardResponse(
                new AdminDashboardResponse.Payments(pendingAmount, getPendingPaymentBatches(batches).size()),
                new AdminDashboardResponse.Verifications(pendingVerifications, upcomingVerifications),
                new AdminDashboardResponse.Inventory(
                        sum(batches, Batch::getQuantityAvailable),
                        sum(batches, Batch::getQuantitySold),
                        sum(batches, Batch::getQuantityWasted)),
                new AdminDashboardResponse.SecondaryCounts(
                        farmerRepository.count(), farmRepository.count(), batchRepository.count()));
    }

    public List<BatchResponse> getPendingPayments() {
        return getPendingPaymentBatches(batchRepository.findAll()).stream().map(BatchResponse::from).toList();
    }

    public List<FarmVerificationResponse> getPendingVerifications() {
        return farmVerificationRepository.findAll().stream().filter(this::isPendingVerification)
                .map(FarmVerificationResponse::from).toList();
    }

    public List<FarmVerificationResponse> getUpcomingVerifications() {
        return farmVerificationRepository.findAll().stream().filter(this::isUpcomingVerification)
                .sorted(java.util.Comparator.comparing(FarmVerification::getNextVerificationDue))
                .map(FarmVerificationResponse::from).toList();
    }

    public List<BatchResponse> getBatchInventory() {
        return batchRepository.findAll().stream().map(BatchResponse::from).toList();
    }

    public List<BatchResponse> getHighWastageBatches() {
        return batchRepository.findAll().stream()
                .filter(batch -> zero(batch.getQuantityWasted()).signum() > 0)
                .sorted(java.util.Comparator.comparing(this::wastageRatio).reversed())
                .map(BatchResponse::from).toList();
    }

    private List<Batch> getPendingPaymentBatches(List<Batch> batches) {
        return batches.stream().filter(batch -> PaymentStatus.UNPAID == batch.getPaymentStatus()).toList();
    }

    private boolean isPendingVerification(FarmVerification verification) {
        return verification.getStatus() != null && verification.getStatus().equalsIgnoreCase("PENDING");
    }

    private boolean isUpcomingVerification(FarmVerification verification) {
        return verification.getNextVerificationDue() != null
                && !verification.getNextVerificationDue().isBefore(LocalDate.now());
    }

    private BigDecimal sum(List<Batch> batches, Function<Batch, BigDecimal> getter) {
        return batches.stream().map(getter).map(this::zero).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private BigDecimal wastageRatio(Batch batch) {
        BigDecimal received = zero(batch.getQuantityReceived());
        return received.signum() == 0 ? BigDecimal.ZERO
                : zero(batch.getQuantityWasted()).divide(received, 6, java.math.RoundingMode.HALF_UP);
    }

    public AdminFarmerOverviewResponse getFarmerOverview(UUID farmerId) {
        Farmer farmer = farmerRepository
                .findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));

        List<Farm> farms = farmRepository.findByFarmerId(farmerId);
        List<Batch> batches = batchRepository.findByFarmerId(farmerId);

        BigDecimal totalPayable = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;

        for (Batch batch : batches) {
            BigDecimal amount = zero(batch.getTotalFarmerAmount());
            totalPayable = totalPayable.add(amount);
            if (PaymentStatus.PAID == batch.getPaymentStatus()) {
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

        List<BatchUsage> usageEntities = batchUsageRepository.findByBatchIdOrderByRecordedAtAsc(batchId);
        List<BatchUsageResponse> usage = usageEntities.stream().map(BatchUsageResponse::from).toList();
        BigDecimal totalSaleAmount = usageEntities.stream()
                .filter(item -> item.getUsageType().isSale() && item.getPricePerUnit() != null)
                .map(item -> item.getQuantity().multiply(item.getPricePerUnit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        AdminBatchSalesSummary salesSummary = new AdminBatchSalesSummary(
                zero(batch.getQuantitySold()), zero(batch.getQuantityAvailable()), totalSaleAmount);

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
                usage,
                salesSummary,
                batch.getMargin(),
                traceEvents,
                qrCode);
    }
}
