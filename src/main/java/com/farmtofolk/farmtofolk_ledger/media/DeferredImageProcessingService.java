package com.farmtofolk.farmtofolk_ledger.media;

import com.farmtofolk.farmtofolk_ledger.events.ImageUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeferredImageProcessingService implements ImageProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DeferredImageProcessingService.class);

    @Override
    public void process(ImageUploadedEvent event) {
        // TODO: Generate compressed/thumbnail variants and persist their URLs when matching columns exist.
        log.debug("Image processing deferred for {} {}", event.entityType(), event.entityId());
    }
}
