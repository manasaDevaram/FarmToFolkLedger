package com.farmtofolk.farmtofolk_ledger.storage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ThumbnailService {

  private static final int MAX_DIMENSION = 400;
  private static final float JPEG_QUALITY = 0.82f;

  private final StorageService storageService;

  public ThumbnailService(StorageService storageService) {
    this.storageService = storageService;
  }

  public void createFromUpload(MultipartFile file, String originalObjectKey) {
    if (file == null || originalObjectKey == null || originalObjectKey.isBlank()) {
      return;
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return;
    }
    try (InputStream inputStream = file.getInputStream()) {
      createFromBytes(inputStream.readAllBytes(), originalObjectKey);
    } catch (IOException exception) {
      throw new StorageException("Thumbnail generation failed");
    }
  }

  public void createFromStoredObject(String originalObjectKey) {
    if (originalObjectKey == null || originalObjectKey.isBlank()) {
      return;
    }
    byte[] originalBytes = storageService.readObjectBytes(originalObjectKey);
    if (originalBytes.length == 0) {
      return;
    }
    createFromBytes(originalBytes, originalObjectKey);
  }

  public String resolveThumbnailPresignedUrl(String originalObjectKey) {
    return storageService.generateThumbnailPresignedUrl(originalObjectKey);
  }

  private void createFromBytes(byte[] imageBytes, String originalObjectKey) {
    String thumbnailKey = ThumbnailKeys.forOriginal(originalObjectKey);
    if (thumbnailKey == null) {
      return;
    }
    if (storageService.objectExists(thumbnailKey)) {
      return;
    }
    byte[] thumbnailBytes = renderThumbnail(imageBytes);
    if (thumbnailBytes.length == 0) {
      return;
    }
    storageService.uploadAtKey(thumbnailBytes, thumbnailKey, "image/jpeg");
  }

  private byte[] renderThumbnail(byte[] imageBytes) {
    try (InputStream inputStream = new ByteArrayInputStream(imageBytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      BufferedImage source = ImageIO.read(inputStream);
      if (source == null) {
        return new byte[0];
      }
      BufferedImage scaled = scaleImage(source);
      writeJpeg(scaled, outputStream);
      return outputStream.toByteArray();
    } catch (IOException exception) {
      return new byte[0];
    }
  }

  private BufferedImage scaleImage(BufferedImage source) {
    int width = source.getWidth();
    int height = source.getHeight();
    if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) {
      return source;
    }
    double scale = Math.min((double) MAX_DIMENSION / width, (double) MAX_DIMENSION / height);
    int targetWidth = Math.max(1, (int) Math.round(width * scale));
    int targetHeight = Math.max(1, (int) Math.round(height * scale));
    Image scaledInstance = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    BufferedImage scaled =
        new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = scaled.createGraphics();
    graphics.drawImage(scaledInstance, 0, 0, null);
    graphics.dispose();
    return scaled;
  }

  private void writeJpeg(BufferedImage image, ByteArrayOutputStream outputStream)
      throws IOException {
    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
    if (!writers.hasNext()) {
      ImageIO.write(image, "jpg", outputStream);
      return;
    }
    ImageWriter writer = writers.next();
    ImageWriteParam writeParam = writer.getDefaultWriteParam();
    if (writeParam.canWriteCompressed()) {
      writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      writeParam.setCompressionQuality(JPEG_QUALITY);
    }
    try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
      writer.setOutput(imageOutputStream);
      writer.write(null, new IIOImage(image, null, null), writeParam);
    } finally {
      writer.dispose();
    }
  }
}
