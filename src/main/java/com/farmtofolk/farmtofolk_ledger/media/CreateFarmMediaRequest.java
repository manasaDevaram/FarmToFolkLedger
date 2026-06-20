package com.farmtofolk.farmtofolk_ledger.media;

public record CreateFarmMediaRequest(
        String mediaType,
        String mediaUrl,
        String caption,
        Boolean isPublic
) {
}
