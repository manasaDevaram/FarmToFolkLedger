package com.farmtofolk.farmtofolk_ledger.media;

import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;
import com.farmtofolk.farmtofolk_ledger.storage.StoredFileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FarmMediaService {

    private final FarmMediaRepository farmMediaRepository;
    private final FarmRepository farmRepository;
    private final PublicTraceCacheService publicTraceCacheService;
    private final StorageService storageService;

    public FarmMediaService(
            FarmMediaRepository farmMediaRepository,
            FarmRepository farmRepository,
            PublicTraceCacheService publicTraceCacheService,
            StorageService storageService
    ) {
        this.farmMediaRepository = farmMediaRepository;
        this.farmRepository = farmRepository;
        this.publicTraceCacheService = publicTraceCacheService;
        this.storageService = storageService;
    }

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
        publicTraceCacheService.evictStableDataForFarm(farmId);
        return FarmMediaResponse.from(savedFarmMedia);
    }

    public FarmMediaResponse uploadFarmMedia(UUID farmId, MultipartFile file, String caption) {
        // Make sure the uploaded media is linked to a real farm.
        verifyFarmExists(farmId);

        // Store the file in S3 and keep only metadata in PostgreSQL.
        StoredFileResponse storedFile = storageService.upload(file, "farm-media/" + farmId);

        FarmMedia farmMedia = new FarmMedia();
        farmMedia.setFarmId(farmId);
        farmMedia.setMediaType(storedFile.contentType());
        farmMedia.setMediaUrl(storedFile.fileUrl());
        farmMedia.setFileKey(storedFile.fileKey());
        farmMedia.setContentType(storedFile.contentType());
        farmMedia.setSizeBytes(storedFile.sizeBytes());
        farmMedia.setCaption(caption);
        farmMedia.setIsPublic(true);

        FarmMedia savedFarmMedia = farmMediaRepository.save(farmMedia);
        // Clear QR page stable data so uploaded media appears in public trace.
        publicTraceCacheService.evictStableDataForFarm(farmId);
        return FarmMediaResponse.from(savedFarmMedia);
    }

    public List<FarmMediaResponse> getMediaForFarm(UUID farmId) {
        // Make sure the farm exists before listing its media.
        verifyFarmExists(farmId);

        // Fetch media oldest first and convert each one to a response.
        return farmMediaRepository.findByFarmIdOrderByCreatedAtAsc(farmId)
                .stream()
                .map(FarmMediaResponse::from)
                .toList();
    }

    public void deleteFarmMedia(UUID mediaId) {
        // Load media first so missing IDs produce the expected message.
        FarmMedia farmMedia = findFarmMedia(mediaId);
        farmMediaRepository.delete(farmMedia);
        // Clear QR page stable data because farm media changed.
        publicTraceCacheService.evictStableDataForFarm(farmMedia.getFarmId());
    }

    private FarmMedia findFarmMedia(UUID mediaId) {
        // Reuse one not-found lookup rule for media delete operations.
        return farmMediaRepository.findById(mediaId)
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
}
