package com.farmtofolk.farmtofolk_ledger.storage;

public record StoredFileResponse(
        String fileKey,
        String fileUrl,
        String originalFilename,
        String contentType,
        Long sizeBytes
) {
}
