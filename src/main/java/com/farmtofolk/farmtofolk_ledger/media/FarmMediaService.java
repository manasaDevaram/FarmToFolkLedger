package com.farmtofolk.farmtofolk_ledger.media;

import com.farmtofolk.farmtofolk_ledger.farm.FarmRepository;
import com.farmtofolk.farmtofolk_ledger.publictrace.PublicTraceCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FarmMediaService {

    private final FarmMediaRepository farmMediaRepository;
    private final FarmRepository farmRepository;
    private final PublicTraceCacheService publicTraceCacheService;

    public FarmMediaService(
            FarmMediaRepository farmMediaRepository,
            FarmRepository farmRepository,
            PublicTraceCacheService publicTraceCacheService
    ) {
        this.farmMediaRepository = farmMediaRepository;
        this.farmRepository = farmRepository;
        this.publicTraceCacheService = publicTraceCacheService;
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
                .orElseThrow(() -> new RuntimeException("Farm media not found"));
    }

    private void verifyFarmExists(UUID farmId) {
        // Prevent creating or listing media for farms that do not exist.
        if (!farmRepository.existsById(farmId)) {
            throw new RuntimeException("Farm not found");
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
