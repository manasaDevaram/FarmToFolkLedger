package com.farmtofolk.farmtofolk_ledger.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public class StorageProperties {

  private String region;
  private String bucket;
  private String accessKey;
  private String secretKey;
  private Integer presignedUrlExpiryMinutes;

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(String bucket) {
    this.bucket = bucket;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public Integer getPresignedUrlExpiryMinutes() {
    return presignedUrlExpiryMinutes;
  }

  public void setPresignedUrlExpiryMinutes(Integer presignedUrlExpiryMinutes) {
    this.presignedUrlExpiryMinutes = presignedUrlExpiryMinutes;
  }
}
