package com.farmtofolk.farmtofolk_ledger.media;

import java.time.LocalDateTime;
import java.util.UUID;
import com.farmtofolk.farmtofolk_ledger.storage.StorageService;

public record FarmMediaResponse(
    UUID id,
    UUID farmId,
    String mediaType,
    String mediaUrl,
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
        farmMedia.getFileKey(),
        farmMedia.getContentType(),
        farmMedia.getSizeBytes(),
        farmMedia.getCaption(),
        farmMedia.getIsPublic(),
        farmMedia.getCreatedAt());
  }

  public static FarmMediaResponse from(FarmMedia farmMedia, StorageService storageService) {
    FarmMediaResponse response = from(farmMedia);
    String storedValue = farmMedia.getFileKey() != null ? farmMedia.getFileKey() : farmMedia.getMediaUrl();
    return new FarmMediaResponse(
        response.id(), response.farmId(), response.mediaType(),
        storageService.generatePresignedUrl(storedValue), response.fileKey(), response.contentType(),
        response.sizeBytes(), response.caption(), response.isPublic(), response.createdAt());
  }

  public FarmMediaResponse withPresignedUrl(StorageService storageService) {
    String storedValue = fileKey != null ? fileKey : mediaUrl;
    return new FarmMediaResponse(id, farmId, mediaType, storageService.generatePresignedUrl(storedValue),
        fileKey, contentType, sizeBytes, caption, isPublic, createdAt);
  }
}
