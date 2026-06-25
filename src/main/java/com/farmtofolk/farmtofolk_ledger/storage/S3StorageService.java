package com.farmtofolk.farmtofolk_ledger.storage;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class S3StorageService implements StorageService {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf"
    );

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    public S3StorageService(S3Client s3Client, StorageProperties storageProperties) {
        this.s3Client = s3Client;
        this.storageProperties = storageProperties;
    }

    @Override
    public StoredFileResponse upload(MultipartFile file, String folderPath) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String contentType = file.getContentType();
        String fileKey = buildFileKey(folderPath, originalFilename);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(fileKey)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | SdkException exception) {
            throw new StorageException("S3 upload failed");
        }

        return new StoredFileResponse(
                fileKey,
                buildFileUrl(fileKey),
                originalFilename,
                contentType,
                file.getSize()
        );
    }

    @Override
    public void delete(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(storageProperties.getBucket())
                .key(fileKey)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkException exception) {
            throw new StorageException("S3 delete failed");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("File too large");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Unsupported content type");
        }

        if (storageProperties.getBucket() == null || storageProperties.getBucket().isBlank()) {
            throw new StorageException("S3 bucket is not configured");
        }
    }

    private String buildFileKey(String folderPath, String originalFilename) {
        LocalDate today = LocalDate.now();
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String normalizedFolder = folderPath.replaceAll("^/+|/+$", "");

        return normalizedFolder + "/" + yearMonth + "/" + UUID.randomUUID() + "-" + sanitizedFilename;
    }

    private String sanitizeFilename(String originalFilename) {
        String sanitized = originalFilename
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9._-]", "");

        if (sanitized.isBlank()) {
            return "file";
        }

        return sanitized;
    }

    private String buildFileUrl(String fileKey) {
        return "https://"
                + storageProperties.getBucket()
                + ".s3."
                + storageProperties.getRegion()
                + ".amazonaws.com/"
                + fileKey;
    }
}
