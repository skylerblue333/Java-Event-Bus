# Sky Java Event Bus

**Status: engineering beta.** A dependency-free Java 21 in-process event bus for bounded synchronous publish/subscribe workflows.

## Implemented behavior

- real Java 21 records/classes with no third-party runtime dependencies
- bounded event IDs, topic names, attributes, and payload sizes
- thread-safe topic registry using concurrent collections
- deterministic subscriber order within each topic
- explicit subscription handles that unsubscribe on `close()`
- subscriber failure isolation: one handler failure is counted without preventing later handlers from receiving the event
- counters for published events, successful deliveries, subscriber failures, and active topics
- hard caps of 1,000 topics and 100 subscribers per topic
- dependency-free test harness for ordering, failure isolation, unsubscribe behavior, and input validation
- CI compiles production/tests with `javac -Xlint:all -Werror`, runs tests, builds a runnable JAR, smoke-tests the CLI, builds a container, and verifies non-root execution

## Verify

```bash
mkdir -p out/main out/test
javac -Xlint:all -Werror -d out/main $(find src/main/java -name '*.java')
javac -Xlint:all -Werror -cp out/main -d out/test $(find src/test/java -name '*.java')
java -cp out/main:out/test com.sky.eventbus.EventBusTest
```

Build a runnable artifact:

```bash
jar --create --file sky-event-bus.jar --main-class com.sky.eventbus.Main -C out/main .
java -jar sky-event-bus.jar
```

## Integration example

```java
var bus = new EventBus();
try (var subscription = bus.subscribe("orders.created", event -> handle(event.payload()))) {
    bus.publish(Event.create("orders.created", Map.of("source", "checkout"), "order-123"));
}
```

## SKYCOIN4444 integration

Use this library for in-process domain events inside a single JVM service: workflow hooks, local cache invalidation, application lifecycle signals, tests, and modular business events. Cross-process or durable ecosystem messaging should use a broker-backed contract through a separate adapter rather than pretending this in-memory bus is distributed infrastructure.

## Explicit limitations

This repository is not Kafka, NATS, RabbitMQ, Pulsar, a durable queue, a distributed log, a transactional outbox, or an exactly-once delivery system. Events exist only during the current process call; there is no persistence, replay, backpressure queue, retry scheduler, process isolation, authentication, authorization, tenant isolation, clustering, HA, or production deployment.

Subscriber code executes synchronously on the publisher's thread. Callers must avoid blocking or untrusted handlers and should move slow work to a verified queue/worker boundary.

See `SECURITY.md` and `CHANGELOG.md` for product and security boundaries.
