package com.farmtofolk.farmtofolk_ledger.storage;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3StorageService implements StorageService {

  private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("image/jpeg", "image/png", "image/webp", "application/pdf");

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final StorageProperties storageProperties;
  private final Duration presignedUrlExpiry;

  public S3StorageService(
      S3Client s3Client,
      S3Presigner s3Presigner,
      StorageProperties storageProperties,
      @Value("${media.presigned-url-expiry-minutes:15}") long expiryMinutes) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.storageProperties = storageProperties;
    this.presignedUrlExpiry = Duration.ofMinutes(expiryMinutes);
  }

  @Override
  public StoredFileResponse upload(MultipartFile file, String folderPath) {
    return upload(file, folderPath, ALLOWED_CONTENT_TYPES);
  }

  @Override
  public StoredFileResponse upload(
      MultipartFile file, String folderPath, Set<String> allowedContentTypes) {
    validateFile(file, allowedContentTypes);

    String originalFilename =
        file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
    String contentType = file.getContentType();
    String fileKey = buildFileKey(folderPath, originalFilename);

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(storageProperties.getBucket())
            .key(fileKey)
            .contentType(contentType)
            .contentLength(file.getSize())
            .build();

    try {
      s3Client.putObject(
          putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (IOException | SdkException exception) {
      throw new StorageException("S3 upload failed");
    }

    return new StoredFileResponse(fileKey, null, originalFilename, contentType, file.getSize());
  }

  @Override
  public String generatePresignedUrl(String objectKey) {
    if (objectKey == null || objectKey.isBlank()) {
      return null;
    }
    String normalizedKey = extractObjectKey(objectKey);
    if (normalizedKey == null) {
      // Preserve externally hosted legacy media; only this bucket's objects are signed.
      return objectKey;
    }
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(storageProperties.getBucket())
        .key(normalizedKey)
        .build();
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(presignedUrlExpiry)
        .getObjectRequest(getObjectRequest)
        .build();
    try {
      return s3Presigner.presignGetObject(presignRequest).url().toString();
    } catch (SdkException exception) {
      throw new StorageException("S3 URL signing failed");
    }
  }

  @Override
  public void delete(String objectKey) {
    if (objectKey == null || objectKey.isBlank()) {
      return;
    }
    String fileKey = extractObjectKey(objectKey);
    if (fileKey == null) return;

    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(storageProperties.getBucket()).key(fileKey).build();

    try {
      s3Client.deleteObject(deleteObjectRequest);
    } catch (SdkException exception) {
      throw new StorageException("S3 delete failed");
    }
  }

  private void validateFile(MultipartFile file, Set<String> allowedContentTypes) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("File must not be empty");
    }

    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
      throw new BadRequestException("File too large");
    }

    if (!allowedContentTypes.contains(file.getContentType())) {
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
    String sanitized =
        originalFilename
            .toLowerCase(Locale.ROOT)
            .replaceAll("\\s+", "-")
            .replaceAll("[^a-z0-9._-]", "");

    if (sanitized.isBlank()) {
      return "file";
    }

    return sanitized;
  }

  private String extractObjectKey(String storedValue) {
    if (!storedValue.startsWith("https://")) {
      return storedValue.replaceFirst("^/+", "");
    }
    try {
      URI uri = URI.create(storedValue);
      String host = uri.getHost();
      String bucket = storageProperties.getBucket();
      String path = uri.getPath().replaceFirst("^/+", "");
      if (host != null && host.startsWith(bucket + ".s3")) {
        return URLDecoder.decode(path, StandardCharsets.UTF_8);
      }
      if (host != null && host.startsWith("s3") && path.startsWith(bucket + "/")) {
        return URLDecoder.decode(path.substring(bucket.length() + 1), StandardCharsets.UTF_8);
      }
      return null;
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }
}
