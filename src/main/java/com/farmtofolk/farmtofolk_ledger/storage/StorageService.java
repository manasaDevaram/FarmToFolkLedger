package com.farmtofolk.farmtofolk_ledger.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface StorageService {

    StoredFileResponse upload(MultipartFile file, String folderPath);

    StoredFileResponse upload(MultipartFile file, String folderPath, Set<String> allowedContentTypes);

    void delete(String fileKey);
}
