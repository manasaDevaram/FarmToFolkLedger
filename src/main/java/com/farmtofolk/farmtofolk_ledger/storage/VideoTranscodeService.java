package com.farmtofolk.farmtofolk_ledger.storage;

import com.farmtofolk.farmtofolk_ledger.common.error.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoTranscodeService {

  private static final Logger log = LoggerFactory.getLogger(VideoTranscodeService.class);
  private static final long TARGET_MAX_BYTES = 15L * 1024 * 1024;
  private static final long TRANSCODE_TIMEOUT_SECONDS = 120;

  private final boolean ffmpegAvailable;

  public VideoTranscodeService() {
    this.ffmpegAvailable = detectFfmpeg();
    if (!ffmpegAvailable) {
      log.warn("ffmpeg not found; uploaded videos will be stored without server-side compression");
    }
  }

  public TranscodedVideo transcodeForUpload(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BadRequestException("File must not be empty");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("video/")) {
      throw new BadRequestException("Unsupported video upload");
    }
    if (!ffmpegAvailable || file.getSize() <= TARGET_MAX_BYTES) {
      return TranscodedVideo.original(file);
    }

    Path inputPath = null;
    Path outputPath = null;
    try {
      inputPath = Files.createTempFile("ftf-video-in-", suffixFor(file.getOriginalFilename()));
      outputPath = Files.createTempFile("ftf-video-out-", ".mp4");
      try (InputStream inputStream = file.getInputStream()) {
        Files.copy(inputStream, inputPath, StandardCopyOption.REPLACE_EXISTING);
      }
      runFfmpeg(inputPath, outputPath);
      byte[] outputBytes = Files.readAllBytes(outputPath);
      if (outputBytes.length == 0) {
        return TranscodedVideo.original(file);
      }
      String outputName = baseName(file.getOriginalFilename()) + ".mp4";
      return new TranscodedVideo(outputBytes, outputName, "video/mp4", outputBytes.length);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      log.warn("Video transcode interrupted; storing original upload", exception);
      return TranscodedVideo.original(file);
    } catch (IOException exception) {
      log.warn("Video transcode failed; storing original upload", exception);
      return TranscodedVideo.original(file);
    } finally {
      deleteQuietly(inputPath);
      deleteQuietly(outputPath);
    }
  }

  private void runFfmpeg(Path inputPath, Path outputPath)
      throws IOException, InterruptedException {
    ProcessBuilder processBuilder =
        new ProcessBuilder(
            "ffmpeg",
            "-y",
            "-i",
            inputPath.toString(),
            "-vf",
            "scale='min(1280,iw)':-2",
            "-c:v",
            "libx264",
            "-preset",
            "fast",
            "-crf",
            "28",
            "-maxrate",
            "2M",
            "-bufsize",
            "4M",
            "-c:a",
            "aac",
            "-b:a",
            "128k",
            "-movflags",
            "+faststart",
            outputPath.toString());
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    boolean finished = process.waitFor(TRANSCODE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    if (!finished) {
      process.destroyForcibly();
      throw new IOException("ffmpeg timed out");
    }
    if (process.exitValue() != 0) {
      throw new IOException("ffmpeg exited with code " + process.exitValue());
    }
  }

  private boolean detectFfmpeg() {
    try {
      Process process = new ProcessBuilder("ffmpeg", "-version").start();
      return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (IOException | InterruptedException exception) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private String suffixFor(String originalFilename) {
    if (originalFilename == null || !originalFilename.contains(".")) {
      return ".bin";
    }
    return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
  }

  private String baseName(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return "video";
    }
    int dot = originalFilename.lastIndexOf('.');
    if (dot <= 0) {
      return originalFilename;
    }
    return originalFilename.substring(0, dot);
  }

  private void deleteQuietly(Path path) {
    if (path == null) {
      return;
    }
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
      // Best-effort temp cleanup.
    }
  }

  public record TranscodedVideo(byte[] content, String filename, String contentType, long sizeBytes) {

    static TranscodedVideo original(MultipartFile file) {
      try {
        return new TranscodedVideo(
            file.getBytes(),
            file.getOriginalFilename() == null ? "video.mp4" : file.getOriginalFilename(),
            file.getContentType() == null ? "video/mp4" : file.getContentType(),
            file.getSize());
      } catch (IOException exception) {
        throw new StorageException("Video upload failed");
      }
    }
  }
}
