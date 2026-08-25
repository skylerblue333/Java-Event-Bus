package com.sky.eventbus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

public final class EventBusTest {
    private EventBusTest() {}

    public static void main(String[] args) throws Exception {
        publishesInSubscriptionOrder();
        isolatesSubscriberFailure();
        unsubscribeStopsDelivery();
        validatesEventBounds();
        System.out.println("event bus tests passed");
    }

    private static void publishesInSubscriptionOrder() throws Exception {
        var bus = new EventBus();
        var seen = new ArrayList<String>();
        var first = bus.subscribe("orders.created", event -> seen.add("first:" + event.payload()));
        var second = bus.subscribe("orders.created", event -> seen.add("second:" + event.payload()));
        try {
            var result = bus.publish(new Event("evt-1", "orders.created", Instant.EPOCH, Map.of(), "42"));
            check(result.delivered() == 2, "expected two deliveries");
            check(result.failed() == 0, "expected no failures");
            check(seen.equals(java.util.List.of("first:42", "second:42")), "subscription order changed");
        } finally {
            second.close();
            first.close();
        }
    }

    private static void isolatesSubscriberFailure() throws Exception {
        var bus = new EventBus();
        var seen = new ArrayList<String>();
        var bad = bus.subscribe("topic", event -> { throw new IllegalStateException("boom"); });
        var good = bus.subscribe("topic", event -> seen.add(event.id()));
        try {
            var result = bus.publish(new Event("evt-2", "topic", Instant.EPOCH, Map.of(), ""));
            check(result.delivered() == 1, "good subscriber was not delivered");
            check(result.failed() == 1, "subscriber failure was not counted");
            check(seen.equals(java.util.List.of("evt-2")), "good subscriber did not run");
        } finally {
            good.close();
            bad.close();
        }
    }

    private static void unsubscribeStopsDelivery() throws Exception {
        var bus = new EventBus();
        var seen = new ArrayList<String>();
        var subscription = bus.subscribe("topic", event -> seen.add(event.id()));
        subscription.close();
        var result = bus.publish(new Event("evt-3", "topic", Instant.EPOCH, Map.of(), ""));
        check(result.delivered() == 0, "closed subscription still received event");
        check(seen.isEmpty(), "closed subscription mutated state");
    }

    private static void validatesEventBounds() {
        expectFailure(() -> new Event("bad id with spaces", "topic", Instant.EPOCH, Map.of(), ""));
        expectFailure(() -> new Event("evt-4", "bad topic!", Instant.EPOCH, Map.of(), ""));
        expectFailure(() -> new Event("evt-5", "topic", Instant.EPOCH, Map.of(), "x".repeat(16_001)));
    }

    private static void expectFailure(Runnable action) {
        try {
            action.run();
            throw new AssertionError("expected failure");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
