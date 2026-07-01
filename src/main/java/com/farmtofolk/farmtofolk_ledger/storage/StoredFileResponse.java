package com.farmtofolk.farmtofolk_ledger.storage;

public record StoredFileResponse(
    String objectKey, String fileUrl, String originalFilename, String contentType, Long sizeBytes) {

  /** Temporary source compatibility while callers move to the clearer objectKey name. */
  public String fileKey() {
    return objectKey;
  }
}
