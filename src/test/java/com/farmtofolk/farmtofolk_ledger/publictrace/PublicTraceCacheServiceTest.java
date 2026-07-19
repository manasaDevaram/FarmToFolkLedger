package com.farmtofolk.farmtofolk_ledger.publictrace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.farmtofolk.farmtofolk_ledger.batch.Batch;
import com.farmtofolk.farmtofolk_ledger.batch.BatchRepository;
import com.farmtofolk.farmtofolk_ledger.farm.Farm;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.farmer.Farmer;
import com.farmtofolk.farmtofolk_ledger.farmer.FarmerRepository;
import com.farmtofolk.farmtofolk_ledger.media.FarmMedia;
import com.farmtofolk.farmtofolk_ledger.media.FarmMediaRepository;
import com.farmtofolk.farmtofolk_ledger.qr.QrCode;
import com.farmtofolk.farmtofolk_ledger.qr.QrCodeRepository;
import com.farmtofolk.farmtofolk_ledger.verification.VerificationEvidenceResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicTraceCacheServiceTest {

  @Mock private QrCodeRepository qrCodeRepository;

  @Mock private BatchRepository batchRepository;

  @Mock private FarmerRepository farmerRepository;

  @Mock private FarmRepository farmRepository;

  @Mock private PublicVerificationResolver publicVerificationResolver;

  @Mock private FarmMediaRepository farmMediaRepository;

  @Mock private org.springframework.cache.CacheManager cacheManager;

  @InjectMocks private PublicTraceCacheService publicTraceCacheService;

  @Test
  void getStableDataFiltersPrivateEvidenceAndMedia() {
    String publicToken = "public-token";
    UUID batchId = UUID.randomUUID();
    UUID farmerId = UUID.randomUUID();
    UUID farmId = UUID.randomUUID();

    QrCode qrCode = new QrCode();
    qrCode.setBatchId(batchId);

    Batch batch = new Batch();
    ReflectionTestUtils.setField(batch, "id", batchId);
    batch.setFarmerId(farmerId);
    batch.setFarmId(farmId);

    Farmer farmer = new Farmer();
    ReflectionTestUtils.setField(farmer, "id", farmerId);
    farmer.setName("Ramesh");

    Farm farm = new Farm();
    ReflectionTestUtils.setField(farm, "id", farmId);
    farm.setFarmName("Public Farm");

    VerificationEvidenceResponse publicEvidence =
        new VerificationEvidenceResponse(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "IMAGE",
            "https://example.com/public.jpg",
            null,
            null,
            null,
            null,
            null,
            "Public evidence",
            true,
            null,
            null,
            null);

    FarmMedia publicMedia = new FarmMedia();
    publicMedia.setFarmId(farmId);
    publicMedia.setMediaUrl("https://example.com/public-media.jpg");
    publicMedia.setIsPublic(true);

    FarmMedia privateMedia = new FarmMedia();
    privateMedia.setFarmId(farmId);
    privateMedia.setMediaUrl("https://example.com/private-media.jpg");
    privateMedia.setIsPublic(false);

    when(qrCodeRepository.findByPublicTokenAndIsActiveTrue(publicToken))
        .thenReturn(Optional.of(qrCode));
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(farmerRepository.findById(farmerId)).thenReturn(Optional.of(farmer));
    when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));
    when(publicVerificationResolver.resolveForFarm(farmId))
        .thenReturn(
            new PublicVerificationResolver.PublicVerificationSnapshot(
                new PublicTraceVerificationResponse(
                    LocalDate.of(2026, 5, 17),
                    "FIELD_VISIT",
                    true,
                    true,
                    "{}",
                    null,
                    null),
                List.of(publicEvidence)));
    when(farmMediaRepository.findByFarmIdOrderByCreatedAtAsc(farmId))
        .thenReturn(List.of(publicMedia, privateMedia));

    CachedPublicTraceStableData stableData = publicTraceCacheService.getStableData(publicToken);

    assertEquals(LocalDate.of(2026, 5, 17), stableData.verification().verificationDate());
    assertEquals(1, stableData.verificationEvidence().size());
    assertEquals(
        "https://example.com/public.jpg", stableData.verificationEvidence().getFirst().fileUrl());
    assertEquals(1, stableData.farmMedia().size());
    assertEquals(
        "https://example.com/public-media.jpg", stableData.farmMedia().getFirst().mediaUrl());
  }

  @Test
  void getStableDataSkipsPendingVerificationForPublicTrace() {
    String publicToken = "public-token";
    UUID batchId = UUID.randomUUID();
    UUID farmerId = UUID.randomUUID();
    UUID farmId = UUID.randomUUID();

    QrCode qrCode = new QrCode();
    qrCode.setBatchId(batchId);

    Batch batch = new Batch();
    ReflectionTestUtils.setField(batch, "id", batchId);
    batch.setFarmerId(farmerId);
    batch.setFarmId(farmId);

    Farmer farmer = new Farmer();
    ReflectionTestUtils.setField(farmer, "id", farmerId);

    Farm farm = new Farm();
    ReflectionTestUtils.setField(farm, "id", farmId);

    when(qrCodeRepository.findByPublicTokenAndIsActiveTrue(publicToken))
        .thenReturn(Optional.of(qrCode));
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(farmerRepository.findById(farmerId)).thenReturn(Optional.of(farmer));
    when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));
    when(publicVerificationResolver.resolveForFarm(farmId))
        .thenReturn(PublicVerificationResolver.PublicVerificationSnapshot.empty());
    when(farmMediaRepository.findByFarmIdOrderByCreatedAtAsc(farmId)).thenReturn(List.of());

    CachedPublicTraceStableData stableData = publicTraceCacheService.getStableData(publicToken);

    assertEquals(null, stableData.verification());
    assertEquals(0, stableData.verificationEvidence().size());
  }

  @Test
  void getStableDataReturnsVerifiedRecordWithoutPublicEvidence() {
    String publicToken = "public-token";
    UUID batchId = UUID.randomUUID();
    UUID farmerId = UUID.randomUUID();
    UUID farmId = UUID.randomUUID();

    QrCode qrCode = new QrCode();
    qrCode.setBatchId(batchId);

    Batch batch = new Batch();
    ReflectionTestUtils.setField(batch, "id", batchId);
    batch.setFarmerId(farmerId);
    batch.setFarmId(farmId);

    Farmer farmer = new Farmer();
    ReflectionTestUtils.setField(farmer, "id", farmerId);

    Farm farm = new Farm();
    ReflectionTestUtils.setField(farm, "id", farmId);

    when(qrCodeRepository.findByPublicTokenAndIsActiveTrue(publicToken))
        .thenReturn(Optional.of(qrCode));
    when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));
    when(farmerRepository.findById(farmerId)).thenReturn(Optional.of(farmer));
    when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));
    when(publicVerificationResolver.resolveForFarm(farmId))
        .thenReturn(
            new PublicVerificationResolver.PublicVerificationSnapshot(
                new PublicTraceVerificationResponse(
                    LocalDate.of(2026, 5, 17),
                    "FIELD_VISIT",
                    true,
                    true,
                    "{}",
                    null,
                    null),
                List.of()));
    when(farmMediaRepository.findByFarmIdOrderByCreatedAtAsc(farmId)).thenReturn(List.of());

    CachedPublicTraceStableData stableData = publicTraceCacheService.getStableData(publicToken);

    assertEquals(LocalDate.of(2026, 5, 17), stableData.verification().verificationDate());
    assertEquals(0, stableData.verificationEvidence().size());
  }
}
