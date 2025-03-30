package com.eventsaggregation.model;

import java.time.LocalDateTime;

public record Event(String userId, LocalDateTime timestamp, String event) {
}

