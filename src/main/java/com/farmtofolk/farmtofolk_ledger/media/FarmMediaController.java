package com.farmtofolk.farmtofolk_ledger.media;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FarmMediaController {

  private final FarmMediaService farmMediaService;

  public FarmMediaController(FarmMediaService farmMediaService) {
    this.farmMediaService = farmMediaService;
  }

  @PostMapping("/api/farms/{farmId}/media")
  @ResponseStatus(HttpStatus.CREATED)
  public FarmMediaResponse createFarmMedia(
      @PathVariable UUID farmId, @Valid @RequestBody CreateFarmMediaRequest request) {
    return farmMediaService.createFarmMedia(farmId, request);
  }

  @PostMapping(
      value = "/api/farms/{farmId}/media/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public FarmMediaResponse uploadFarmMedia(
      @PathVariable UUID farmId,
      @RequestParam MultipartFile file,
      @RequestParam(required = false) String caption) {
    return farmMediaService.uploadFarmMedia(farmId, file, caption);
  }

  @GetMapping("/api/farms/{farmId}/media")
  public List<FarmMediaResponse> getMediaForFarm(@PathVariable UUID farmId) {
    return farmMediaService.getMediaForFarm(farmId);
  }

  @DeleteMapping("/api/farm-media/{mediaId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteFarmMedia(@PathVariable UUID mediaId) {
    farmMediaService.deleteFarmMedia(mediaId);
  }
}
