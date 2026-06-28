package com.farmtofolk.farmtofolk_ledger.farmer;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/farmers")
public class FarmerController {

  private final FarmerService farmerService;

  public FarmerController(FarmerService farmerService) {
    this.farmerService = farmerService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FarmerResponse createFarmer(@Valid @RequestBody CreateFarmerRequest request) {
    return farmerService.createFarmer(request);
  }

  @GetMapping("/{farmerId}")
  public FarmerResponse getFarmer(@PathVariable UUID farmerId) {
    return farmerService.getFarmer(farmerId);
  }

  @GetMapping
  public List<FarmerResponse> getAllFarmers() {
    return farmerService.getAllFarmers();
  }

  @PutMapping("/{farmerId}")
  public FarmerResponse updateFarmer(
      @PathVariable UUID farmerId, @Valid @RequestBody CreateFarmerRequest request) {
    return farmerService.updateFarmer(farmerId, request);
  }

  @PatchMapping("/{farmerId}/status")
  public FarmerResponse updateFarmerStatus(
      @PathVariable UUID farmerId, @Valid @RequestBody UpdateFarmerStatusRequest request) {
    return farmerService.updateFarmerStatus(farmerId, request);
  }

  @PostMapping(
      value = "/{farmerId}/profile-photo/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public FarmerResponse uploadProfilePhoto(
      @PathVariable UUID farmerId, @RequestParam MultipartFile file) {
    return farmerService.uploadProfilePhoto(farmerId, file);
  }

  @PostMapping(
      value = "/{farmerId}/intro-video/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public FarmerResponse uploadIntroVideo(
      @PathVariable UUID farmerId, @RequestParam MultipartFile file) {
    return farmerService.uploadIntroVideo(farmerId, file);
  }
}
