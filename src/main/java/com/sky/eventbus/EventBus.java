package com.sky.eventbus;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class EventBus {
    public static final int MAX_TOPICS = 1_000;
    public static final int MAX_SUBSCRIBERS_PER_TOPIC = 100;

    private final Map<String, CopyOnWriteArrayList<Consumer<Event>>> subscribers = new ConcurrentHashMap<>();
    private final AtomicLong published = new AtomicLong();
    private final AtomicLong deliveries = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();

    public AutoCloseable subscribe(String topic, Consumer<Event> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        validateTopic(topic);
        if (!subscribers.containsKey(topic) && subscribers.size() >= MAX_TOPICS) {
            throw new IllegalStateException("topic capacity reached");
        }
        var handlers = subscribers.computeIfAbsent(topic, ignored -> new CopyOnWriteArrayList<>());
        if (handlers.size() >= MAX_SUBSCRIBERS_PER_TOPIC) {
            throw new IllegalStateException("subscriber capacity reached for topic");
        }
        handlers.add(consumer);
        return () -> unsubscribe(topic, consumer);
    }

    public PublishResult publish(Event event) {
        Objects.requireNonNull(event, "event");
        published.incrementAndGet();
        List<Consumer<Event>> handlers = subscribers.getOrDefault(event.topic(), new CopyOnWriteArrayList<>());
        int delivered = 0;
        int failed = 0;
        for (Consumer<Event> handler : handlers) {
            try {
                handler.accept(event);
                delivered++;
                deliveries.incrementAndGet();
            } catch (RuntimeException exception) {
                failed++;
                failures.incrementAndGet();
            }
        }
        return new PublishResult(event.id(), event.topic(), delivered, failed);
    }

    public Metrics metrics() {
        return new Metrics(published.get(), deliveries.get(), failures.get(), subscribers.size());
    }

    private void unsubscribe(String topic, Consumer<Event> consumer) {
        var handlers = subscribers.get(topic);
        if (handlers == null) {
            return;
        }
        handlers.remove(consumer);
        if (handlers.isEmpty()) {
            subscribers.remove(topic, handlers);
        }
    }

    private static void validateTopic(String topic) {
        if (topic == null || !topic.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,99}")) {
            throw new IllegalArgumentException("topic must be 1-100 safe characters");
        }
    }

    public record PublishResult(String eventId, String topic, int delivered, int failed) {}

    public record Metrics(long published, long deliveries, long failures, int activeTopics) {}
}
