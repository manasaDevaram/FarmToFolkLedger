package com.farmtofolk.farmtofolk_ledger.events;

import com.farmtofolk.farmtofolk_ledger.media.ImageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ImageProcessingListener {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingListener.class);

    private final ImageProcessingService imageProcessingService;

    public ImageProcessingListener(ImageProcessingService imageProcessingService) {
        this.imageProcessingService = imageProcessingService;
    }

    @Async("domainEventExecutor")
    @EventListener
    public void onImageUploaded(ImageUploadedEvent event) {
        try {
            imageProcessingService.process(event);
        } catch (RuntimeException exception) {
            log.warn("Failed to process uploaded image for {} {}", event.entityType(), event.entityId(), exception);
        }
    }
}
