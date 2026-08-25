package com.sky.eventbus;

import java.util.Map;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        var bus = new EventBus();
        var event = Event.create("system.health", Map.of("source", "cli"), "ok");
        var result = bus.publish(event);
        System.out.printf(
            "{\"service\":\"sky-java-event-bus\",\"eventId\":\"%s\",\"delivered\":%d,\"failed\":%d}%n",
            result.eventId(),
            result.delivered(),
            result.failed()
        );
    }
}
