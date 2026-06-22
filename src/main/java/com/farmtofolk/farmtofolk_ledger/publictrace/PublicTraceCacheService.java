package com.farmtofolk.farmtofolk_ledger.publictrace;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.batch.BatchResponse;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farm.FarmResponse;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerResponse;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaResponse;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerification;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationRepository;
import com.farmtofolk.farmtofolk_ledger.verification.FarmVerificationResponse;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceResponse;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PublicTraceCacheService {

    private final QrCodeRepository qrCodeRepository;
    private final BatchRepository batchRepository;
    private final FarmerRepository farmerRepository;
    private final FarmRepository farmRepository;
    private final FarmVerificationRepository farmVerificationRepository;
    private final VerificationEvidenceRepository verificationEvidenceRepository;
    private final FarmMediaRepository farmMediaRepository;
    private final CacheManager cacheManager;

    public PublicTraceCacheService(
            QrCodeRepository qrCodeRepository,
            BatchRepository batchRepository,
            FarmerRepository farmerRepository,
            FarmRepository farmRepository,
            FarmVerificationRepository farmVerificationRepository,
            VerificationEvidenceRepository verificationEvidenceRepository,
            FarmMediaRepository farmMediaRepository,
            CacheManager cacheManager
    ) {
        this.qrCodeRepository = qrCodeRepository;
        this.batchRepository = batchRepository;
        this.farmerRepository = farmerRepository;
        this.farmRepository = farmRepository;
        this.farmVerificationRepository = farmVerificationRepository;
        this.verificationEvidenceRepository = verificationEvidenceRepository;
        this.farmMediaRepository = farmMediaRepository;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "publicTraceStable", key = "#publicToken")
    public CachedPublicTraceStableData getStableData(String publicToken) {
        // Resolve the QR token so stable data can be loaded from the batch.
        QrCode qrCode = qrCodeRepository.findByPublicTokenAndIsActiveTrue(publicToken)
                .orElseThrow(() -> new RuntimeException("QR code not found"));

        // Load the required stable trace objects.
        Batch batch = batchRepository.findById(qrCode.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch not found"));
        Farmer farmer = farmerRepository.findById(batch.getFarmerId())
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        Farm farm = farmRepository.findById(batch.getFarmId())
                .orElseThrow(() -> new RuntimeException("Farm not found"));

        // Load optional verification and evidence data when available.
        FarmVerification latestVerification = farmVerificationRepository
                .findFirstByFarmIdOrderByVerificationDateDesc(farm.getId())
                .orElse(null);
        List<VerificationEvidenceResponse> verificationEvidence = latestVerification == null
                ? List.of()
                : verificationEvidenceRepository
                        .findByVerificationIdOrderByCreatedAtAsc(latestVerification.getId())
                        .stream()
                        .map(VerificationEvidenceResponse::from)
                        .toList();

        // Load farm media as stable public trace content.
        List<FarmMediaResponse> farmMedia = farmMediaRepository.findByFarmIdOrderByCreatedAtAsc(farm.getId())
                .stream()
                .map(FarmMediaResponse::from)
                .toList();

        return new CachedPublicTraceStableData(
                BatchResponse.from(batch),
                FarmerResponse.from(farmer),
                FarmResponse.from(farm),
                latestVerification == null ? null : FarmVerificationResponse.from(latestVerification),
                verificationEvidence,
                farmMedia
        );
    }

    @CacheEvict(value = "publicTraceStable", key = "#publicToken")
    public void evictStableData(String publicToken) {
        // Explicit hook for future write flows that need to invalidate public trace stable data.
    }

    public void evictStableDataForBatch(UUID batchId) {
        // Evict the public trace cache connected to this batch's active QR token.
        qrCodeRepository.findFirstByBatchIdAndIsActiveTrue(batchId)
                .map(QrCode::getPublicToken)
                .ifPresent(this::evictStableDataSafely);
    }

    public void evictStableDataForFarmer(UUID farmerId) {
        // Evict every public trace cache for batches owned by this farmer.
        batchRepository.findByFarmerId(farmerId)
                .forEach(batch -> evictStableDataForBatch(batch.getId()));
    }

    public void evictStableDataForFarm(UUID farmId) {
        // Evict every public trace cache for batches produced by this farm.
        batchRepository.findByFarmId(farmId)
                .forEach(batch -> evictStableDataForBatch(batch.getId()));
    }

    private void evictStableDataSafely(String publicToken) {
        try {
            Cache cache = cacheManager.getCache("publicTraceStable");
            if (cache != null) {
                cache.evict(publicToken);
            }
        } catch (RuntimeException ignored) {
            // Redis/cache eviction failures should not block the database write.
        }
    }
}
