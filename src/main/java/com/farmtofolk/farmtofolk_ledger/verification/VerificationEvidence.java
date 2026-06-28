package com.farmtofolk.farmtofolk_ledger.verification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_evidence")
public class VerificationEvidence {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "verification_id", nullable = false)
  private UUID verificationId;

  @Column(name = "file_type")
  private String fileType;

  @Column(name = "file_url", nullable = false)
  private String fileUrl;

  @Column(name = "file_key")
  private String fileKey;

  @Column(name = "file_hash")
  private String fileHash;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "size_bytes")
  private Long sizeBytes;

  @Column(columnDefinition = "TEXT")
  private String caption;

  @Column(name = "is_public")
  private Boolean isPublic = true;

  @Column(name = "captured_at")
  private LocalDateTime capturedAt;

  @Column(name = "uploaded_by_user_id")
  private UUID uploadedByUserId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  void prePersist() {
    createdAt = LocalDateTime.now();

    if (isPublic == null) {
      isPublic = true;
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getVerificationId() {
    return verificationId;
  }

  public void setVerificationId(UUID verificationId) {
    this.verificationId = verificationId;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public String getFileUrl() {
    return fileUrl;
  }

  public void setFileUrl(String fileUrl) {
    this.fileUrl = fileUrl;
  }

  public String getFileKey() {
    return fileKey;
  }

  public void setFileKey(String fileKey) {
    this.fileKey = fileKey;
  }

  public String getFileHash() {
    return fileHash;
  }

  public void setFileHash(String fileHash) {
    this.fileHash = fileHash;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Long getSizeBytes() {
    return sizeBytes;
  }

  public void setSizeBytes(Long sizeBytes) {
    this.sizeBytes = sizeBytes;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public Boolean getIsPublic() {
    return isPublic;
  }

  public void setIsPublic(Boolean isPublic) {
    this.isPublic = isPublic;
  }

  public LocalDateTime getCapturedAt() {
    return capturedAt;
  }

  public void setCapturedAt(LocalDateTime capturedAt) {
    this.capturedAt = capturedAt;
  }

  public UUID getUploadedByUserId() {
    return uploadedByUserId;
  }

  public void setUploadedByUserId(UUID uploadedByUserId) {
    this.uploadedByUserId = uploadedByUserId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
