package com.farmtofolk.farmtofolk_ledger.media;

import com.farmtofolk.farmtofolk_ledger.events.ImageUploadedEvent;
import com.farmtofolk.farmtofolk_ledger.storage.ThumbnailService;
import org.springframework.stereotype.Service;

@Service
public class DeferredImageProcessingService implements ImageProcessingService {

  private final ThumbnailService thumbnailService;

  public DeferredImageProcessingService(ThumbnailService thumbnailService) {
    this.thumbnailService = thumbnailService;
  }

  @Override
  public void process(ImageUploadedEvent event) {
    thumbnailService.createFromStoredObject(event.originalUrl());
  }
}
