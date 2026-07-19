package com.farmtofolk.farmtofolk_ledger.media;

import java.time.LocalDateTime;
import java.util.UUID;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;

public record FarmMediaResponse(
    UUID id,
    UUID farmId,
    String mediaType,
    String mediaUrl,
    String thumbnailUrl,
    String fileKey,
    String contentType,
    Long sizeBytes,
    String caption,
    Boolean isPublic,
    LocalDateTime createdAt) {

  public static FarmMediaResponse from(FarmMedia farmMedia) {
    return new FarmMediaResponse(
        farmMedia.getId(),
        farmMedia.getFarmId(),
        farmMedia.getMediaType(),
        farmMedia.getMediaUrl(),
        null,
        farmMedia.getFileKey(),
        farmMedia.getContentType(),
        farmMedia.getSizeBytes(),
        farmMedia.getCaption(),
        farmMedia.getIsPublic(),
        farmMedia.getCreatedAt());
  }

  public static FarmMediaResponse from(FarmMedia farmMedia, StorageService storageService) {
    return from(farmMedia).withPresignedUrl(storageService);
  }

  public FarmMediaResponse withPresignedUrl(StorageService storageService) {
    String storedValue = fileKey != null ? fileKey : mediaUrl;
    return new FarmMediaResponse(
        id,
        farmId,
        mediaType,
        storageService.generatePresignedUrl(storedValue),
        storageService.generateThumbnailPresignedUrl(storedValue),
        fileKey,
        contentType,
        sizeBytes,
        caption,
        isPublic,
        createdAt);
  }
}
