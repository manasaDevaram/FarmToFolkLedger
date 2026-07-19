package com.farmtofolk.farmtofolk_ledger.storage;

import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  StoredFileResponse upload(MultipartFile file, String folderPath);

  StoredFileResponse upload(MultipartFile file, String folderPath, Set<String> allowedContentTypes);

  StoredFileResponse upload(
      byte[] content, String originalFilename, String contentType, String folderPath);

  void uploadAtKey(byte[] content, String objectKey, String contentType);

  boolean objectExists(String objectKey);

  byte[] readObjectBytes(String objectKey);

  String generatePresignedUrl(String objectKey);

  String generateThumbnailPresignedUrl(String objectKey);

  void delete(String objectKey);
}
