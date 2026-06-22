package com.farmtofolk.farmtofolk_ledger.farm;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @PostMapping("/api/farms")
    @ResponseStatus(HttpStatus.CREATED)
    public FarmResponse createFarm(@Valid @RequestBody CreateFarmRequest request) {
        return farmService.createFarm(request);
    }

    @GetMapping("/api/farms/{farmId}")
    public FarmResponse getFarm(@PathVariable UUID farmId) {
        return farmService.getFarm(farmId);
    }

    @GetMapping("/api/farmers/{farmerId}/farms")
    public List<FarmResponse> getFarmsByFarmer(@PathVariable UUID farmerId) {
        return farmService.getFarmsByFarmer(farmerId);
    }

    @PutMapping("/api/farms/{farmId}")
    public FarmResponse updateFarm(
            @PathVariable UUID farmId,
            @Valid @RequestBody CreateFarmRequest request
    ) {
        return farmService.updateFarm(farmId, request);
    }
}
