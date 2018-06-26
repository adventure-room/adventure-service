package com.programyourhome.adventureroom.model.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
     * Hashcode based on all fields in the subclass.
     * Excludes id, timestamp and message.
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "id", "timestamp", "message");
    }

    /**
     * Equals based on all fields in the subclass.
     * Excludes id, timestamp and message.
     */
    @Override
    public boolean equals(Object object) {
        return EqualsBuilder.reflectionEquals(this, object, "id", "timestamp", "message");
    }

}
