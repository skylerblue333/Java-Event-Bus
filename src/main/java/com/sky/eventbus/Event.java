package com.sky.eventbus;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record Event(
    String id,
    String topic,
    Instant occurredAt,
    Map<String, String> attributes,
    String payload
) {
    public static final int MAX_TOPIC_LENGTH = 100;
    public static final int MAX_ATTRIBUTES = 50;
    public static final int MAX_ATTRIBUTE_LENGTH = 500;
    public static final int MAX_PAYLOAD_LENGTH = 16_000;

    public Event {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(attributes, "attributes");
        Objects.requireNonNull(payload, "payload");
        if (!id.matches("[A-Za-z0-9._:-]{1,128}")) {
            throw new IllegalArgumentException("id must be 1-128 safe characters");
        }
        if (!topic.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,99}")) {
            throw new IllegalArgumentException("topic must be 1-100 safe characters");
        }
        if (attributes.size() > MAX_ATTRIBUTES) {
            throw new IllegalArgumentException("attributes exceed " + MAX_ATTRIBUTES);
        }
        attributes.forEach((key, value) -> {
            if (key == null || value == null || key.isBlank() || key.length() > 100 || value.length() > MAX_ATTRIBUTE_LENGTH) {
                throw new IllegalArgumentException("invalid event attribute");
            }
        });
        attributes = Map.copyOf(attributes);
        if (payload.length() > MAX_PAYLOAD_LENGTH) {
            throw new IllegalArgumentException("payload exceeds " + MAX_PAYLOAD_LENGTH + " characters");
        }
    }

    public static Event create(String topic, Map<String, String> attributes, String payload) {
        return new Event(UUID.randomUUID().toString(), topic, Instant.now(), attributes, payload);
    }
}
