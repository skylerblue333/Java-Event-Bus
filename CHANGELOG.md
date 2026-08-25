# Changelog

## 0.1.0 - 2026-08-24

- Replace the mislabeled Python placeholder with a real dependency-free Java 21 event bus.
- Add bounded event/topic/attribute/payload validation and topic/subscriber capacity limits.
- Add deterministic synchronous publish order, explicit unsubscribe handles, subscriber-failure isolation, and metrics.
- Add a dependency-free Java test harness and strict `javac -Xlint:all -Werror` CI gates.
- Add runnable JAR smoke testing, non-root Java container packaging, and truthful security/product boundaries.
- Remove obsolete Python and npm runtime scaffolding from the active product.
