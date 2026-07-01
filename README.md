# FarmToFolkLedger
Farm to Folk Traceability platform

## Internal monolith pub/sub

The backend uses Spring `ApplicationEventPublisher`, `@EventListener`, and a bounded async executor
for internal side effects such as scan recording, cache eviction, analytics aggregation, QR image
generation, and image-processing hooks. Events are published through `DomainEventPublisher` only
after the surrounding database transaction commits.

This is in-process monolith pub/sub. It does not use Kafka, RabbitMQ, Redis Streams, or another
external queue, so events are not durable across process crashes. The event contracts are intended
to make a later move to durable messaging straightforward without changing API response shapes.
