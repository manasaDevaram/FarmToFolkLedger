package com.farmtofolk.farmtofolk_ledger.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StoredFileResponse upload(MultipartFile file, String folderPath);

    void delete(String fileKey);
}
