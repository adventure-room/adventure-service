package com.programyourhome.adventureroom.model.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;

public abstract class Event {

    private final UUID id;
    private final LocalDateTime timestamp;
    private final String message;

    public Event(String message) {
        this.id = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    public UUID getId() {
        return this.id;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getMessage() {
        return this.message;
    }

    /**
     * Equals based on all fields in the subclass.
     * Excludes id, timestamp and message.
     */
    public boolean paramEquals(Event event) {
        return EqualsBuilder.reflectionEquals(this, event, "id", "timestamp", "message");
    }

}
