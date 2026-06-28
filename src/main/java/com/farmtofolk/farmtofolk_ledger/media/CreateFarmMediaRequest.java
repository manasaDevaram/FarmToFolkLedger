package com.farmtofolk.farmtofolk_ledger.media;

import jakarta.validation.constraints.NotBlank;

public record CreateFarmMediaRequest(
    @NotBlank String mediaType, @NotBlank String mediaUrl, String caption, Boolean isPublic) {}
