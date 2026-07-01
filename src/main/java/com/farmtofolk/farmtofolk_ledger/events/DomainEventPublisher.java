package com.farmtofolk.farmtofolk_ledger.events;

import com.farmtofolk.farmtofolk_ledger.common.transaction.AfterCommitExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final AfterCommitExecutor afterCommitExecutor;

    public DomainEventPublisher(
            ApplicationEventPublisher applicationEventPublisher,
            AfterCommitExecutor afterCommitExecutor
    ) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.afterCommitExecutor = afterCommitExecutor;
    }

    public void publishAfterCommit(Object event) {
        afterCommitExecutor.run(() -> {
            try {
                applicationEventPublisher.publishEvent(event);
            } catch (RuntimeException exception) {
                log.warn("Failed to publish domain event {}", event.getClass().getSimpleName(), exception);
            }
        });
    }

    public void publishNow(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
