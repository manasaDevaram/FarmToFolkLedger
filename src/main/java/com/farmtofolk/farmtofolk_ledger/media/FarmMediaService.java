package com.farmtofolk.farmtofolk_ledger.media;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.storage.StoredFileResponse;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FarmMediaService {

  private static final Set<String> FARM_MEDIA_CONTENT_TYPES =
      Set.of("image/jpeg", "image/png", "image/webp", "video/mp4", "video/quicktime");

  private final FarmMediaRepository farmMediaRepository;
  private final FarmRepository farmRepository;
  private final PublicTraceCacheService publicTraceCacheService;
  private final StorageService storageService;
  private final AfterCommitExecutor afterCommitExecutor;
  private final TransactionTemplate transactionTemplate;

  public FarmMediaService(
      FarmMediaRepository farmMediaRepository,
      FarmRepository farmRepository,
      PublicTraceCacheService publicTraceCacheService,
      StorageService storageService,
      AfterCommitExecutor afterCommitExecutor,
      PlatformTransactionManager transactionManager) {
    this.farmMediaRepository = farmMediaRepository;
    this.farmRepository = farmRepository;
    this.publicTraceCacheService = publicTraceCacheService;
    this.storageService = storageService;
    this.afterCommitExecutor = afterCommitExecutor;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.transactionTemplate.setPropagationBehavior(
        TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @Transactional
  public FarmMediaResponse createFarmMedia(UUID farmId, CreateFarmMediaRequest request) {
    // Make sure the media is linked to a real farm.
    verifyFarmExists(farmId);

    // Copy request data into a new FarmMedia entity.
    FarmMedia farmMedia = new FarmMedia();
    farmMedia.setFarmId(farmId);
    applyRequest(farmMedia, request);

    // Save the media and return API-friendly response data.
    FarmMedia savedFarmMedia = farmMediaRepository.save(farmMedia);
    // Clear QR page stable data because farm media changed.
    afterCommitExecutor.run(() -> publicTraceCacheService.evictStableDataForFarm(farmId));
    return FarmMediaResponse.from(savedFarmMedia, storageService);
  }

  public FarmMediaResponse uploadFarmMedia(UUID farmId, MultipartFile file, String caption) {
    // Make sure the uploaded media is linked to a real farm.
    verifyFarmExists(farmId);

    // Store the file in S3 and keep only metadata in PostgreSQL.
    StoredFileResponse storedFile =
        storageService.upload(file, "farm-media/" + farmId, FARM_MEDIA_CONTENT_TYPES);

    FarmMediaResponse response;
    try {
      response =
          transactionTemplate.execute(
              status -> {
                FarmMedia farmMedia = new FarmMedia();
                farmMedia.setFarmId(farmId);
                farmMedia.setMediaType(storedFile.contentType());
                farmMedia.setMediaUrl(storedFile.objectKey());
                farmMedia.setFileKey(storedFile.objectKey());
                farmMedia.setContentType(storedFile.contentType());
                farmMedia.setSizeBytes(storedFile.sizeBytes());
                farmMedia.setCaption(caption);
                farmMedia.setIsPublic(true);
                return FarmMediaResponse.from(farmMediaRepository.save(farmMedia), storageService);
              });
    } catch (RuntimeException exception) {
      deleteUploadedFileSafely(storedFile.fileKey());
      throw exception;
    }

    // TransactionTemplate has committed before the public cache is invalidated.
    publicTraceCacheService.evictStableDataForFarm(farmId);
    return response;
  }

  @Transactional(readOnly = true)
  public List<FarmMediaResponse> getMediaForFarm(UUID farmId) {
    // Make sure the farm exists before listing its media.
    verifyFarmExists(farmId);

    // Fetch media oldest first and convert each one to a response.
    return farmMediaRepository.findByFarmIdOrderByCreatedAtAsc(farmId).stream()
        .map(media -> FarmMediaResponse.from(media, storageService))
        .toList();
  }

  @Transactional
  public void deleteFarmMedia(UUID mediaId) {
    // Load media first so missing IDs produce the expected message.
    FarmMedia farmMedia = findFarmMedia(mediaId);
    farmMediaRepository.delete(farmMedia);
    // Clear QR page stable data because farm media changed.
    afterCommitExecutor.run(
        () -> publicTraceCacheService.evictStableDataForFarm(farmMedia.getFarmId()));
  }

  private FarmMedia findFarmMedia(UUID mediaId) {
    // Reuse one not-found lookup rule for media delete operations.
    return farmMediaRepository
        .findById(mediaId)
        .orElseThrow(() -> new ResourceNotFoundException("Farm media not found"));
  }

  private void verifyFarmExists(UUID farmId) {
    // Prevent creating or listing media for farms that do not exist.
    if (!farmRepository.existsById(farmId)) {
      throw new ResourceNotFoundException("Farm not found");
    }
  }

  private void applyRequest(FarmMedia farmMedia, CreateFarmMediaRequest request) {
    // Keep request-to-entity field mapping in one place.
    farmMedia.setMediaType(request.mediaType());
    farmMedia.setMediaUrl(request.mediaUrl());
    farmMedia.setCaption(request.caption());
    farmMedia.setIsPublic(request.isPublic());
  }

  private void deleteUploadedFileSafely(String fileKey) {
    try {
      storageService.delete(fileKey);
    } catch (RuntimeException ignored) {
      // Preserve the database exception; failed cleanup can be retried operationally.
    }
  }
}
