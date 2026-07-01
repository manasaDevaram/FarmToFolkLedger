package com.farmtofolk.farmtofolk_ledger.storage;

import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  StoredFileResponse upload(MultipartFile file, String folderPath);

  StoredFileResponse upload(MultipartFile file, String folderPath, Set<String> allowedContentTypes);

  String generatePresignedUrl(String objectKey);

  void delete(String objectKey);
}
