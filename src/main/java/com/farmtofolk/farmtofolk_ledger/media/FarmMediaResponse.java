package com.farmtofolk.farmtofolk_ledger.media;

import java.time.LocalDateTime;
import java.util.UUID;

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
        LocalDateTime createdAt
) {

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
                farmMedia.getCreatedAt()
        );
    }
}
