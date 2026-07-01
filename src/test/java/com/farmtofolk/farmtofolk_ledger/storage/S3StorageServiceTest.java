package com.farmtofolk.farmtofolk_ledger.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class S3StorageServiceTest {

  private S3Presigner presigner;
  private S3StorageService service;

  @BeforeEach
  void setUp() {
    StorageProperties properties = new StorageProperties();
    properties.setBucket("private-media-bucket");
    properties.setRegion("ap-south-1");
    StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
        AwsBasicCredentials.create("test-access-key", "test-secret-key"));
    presigner = S3Presigner.builder()
        .region(Region.AP_SOUTH_1)
        .credentialsProvider(credentials)
        .build();
    service = new S3StorageService(mock(S3Client.class), presigner, properties, 15);
  }

  @AfterEach
  void tearDown() {
    presigner.close();
  }

  @Test
  void signsObjectKeyForConfiguredFifteenMinutes() {
    String url = service.generatePresignedUrl("farmers/123/profile-photo/2026/07/photo.png");

    assertTrue(url.startsWith("https://private-media-bucket.s3.ap-south-1.amazonaws.com/"));
    assertTrue(url.contains("X-Amz-Expires=900"));
    assertTrue(url.contains("X-Amz-Signature="));
  }

  @Test
  void convertsLegacyBucketUrlToSignedObjectRequest() {
    String url = service.generatePresignedUrl(
        "https://private-media-bucket.s3.ap-south-1.amazonaws.com/farmers/123/photo.png");

    assertTrue(url.contains("/farmers/123/photo.png?"));
    assertTrue(url.contains("X-Amz-Signature="));
  }

  @Test
  void leavesExternalLegacyUrlUntouched() {
    String external = "https://cdn.example.com/photo.png";
    assertEquals(external, service.generatePresignedUrl(external));
  }
}
