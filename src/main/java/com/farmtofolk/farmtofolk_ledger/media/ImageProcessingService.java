package com.farmtofolk.farmtofolk_ledger.media;

import com.farmtofolk.farmtofolk_ledger.events.ImageUploadedEvent;

public interface ImageProcessingService {

    void process(ImageUploadedEvent event);
}
