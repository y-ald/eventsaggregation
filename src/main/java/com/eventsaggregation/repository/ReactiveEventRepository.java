package com.eventsaggregation.repository;

import com.eventsaggregation.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ReactiveEventRepository {
    private static final Logger logger = LoggerFactory.getLogger(ReactiveEventRepository.class);

    // In-memory store for events, indexed by userId, timestamp, and event type.
    private final ConcurrentMap<String, ConcurrentMap<LocalDateTime, ConcurrentMap<String, Event>>> store = new ConcurrentHashMap<>();

    /**
     * Saves an event to the in-memory store.
     *
     * @param event The event to be saved.
     * @return A Mono signaling the completion of the save operation.
     */
    public Mono<Void> save(Event event) {
        logger.debug("Inserting Event: {} in the store", event);
        return Mono.fromRunnable(() -> {
            store.computeIfAbsent(event.userId(), k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(event.timestamp(), k -> new ConcurrentHashMap<>())
                    .put(event.event(), event);
        });
    }

    /**
     * Deletes all events from the in-memory store.
     */
    public void deleteALl() {
        store.clear();
        logger.debug("All store events have been deleted");
    }

    /**
     * Finds events matching the specified criteria from the in-memory store.
     *
     * @param from   The start time of the period to search for events.
     * @param to     The end time of the period to search for events.
     * @param event  The event type to search for.
     * @param userId The ID of the user whose events to search for.
     * @return A Flux emitting the events that match the criteria.
     */
    public Flux<Event> findEvents(LocalDateTime from, LocalDateTime to, String event, String userId) {
        return Flux.fromStream(store.entrySet().stream())
                .filter(entry -> userId == null || entry.getKey().equals(userId))
                .flatMap(entry -> Flux.fromStream(entry.getValue().entrySet().stream()))
                .filter(entry -> !entry.getKey().isBefore(from) && !entry.getKey().isAfter(to))
                .flatMap(entry -> Flux.fromStream(entry.getValue().entrySet().stream()))
                .filter(entry -> event == null || entry.getKey().equals(event))
                .map(entry -> entry.getValue());
    }

    /**
     * Finds distinct users who performed a specific event within the given time period.
     *
     * @param from  The start time of the period to search for events.
     * @param to    The end time of the period to search for events.
     * @param event The event type to search for.
     * @return A Flux emitting the distinct user IDs who match the criteria.
     */
    public Flux<String> findDistinctUsers(LocalDateTime from, LocalDateTime to, String event) {
        return Flux.fromStream(store.entrySet().stream())
                .flatMap(entry -> Flux.fromStream(entry.getValue().entrySet().stream()))
                .filter(entry -> !entry.getKey().isBefore(from) && !entry.getKey().isAfter(to))
                .flatMap(entry -> Flux.fromStream(entry.getValue().entrySet().stream()))
                .filter(entry -> event == null || entry.getKey().equals(event))
                .map(entry -> entry.getValue().userId())
                .distinct();
    }

    /**
     * Checks if a specific event exists for a given user in the in-memory store.
     *
     * @param userId The ID of the user to check for the event.
     * @param event  The event type to check for.
     * @return A Mono emitting true if the event exists for the user, false otherwise.
     */
    public Mono<Boolean> existsByUserIdAndEvent(String userId, String event) {
        return Mono.justOrEmpty(store.get(userId))
                .flatMap(map -> Flux.fromIterable(map.values())
                        .flatMap(events -> Mono.just(events.containsKey(event)))
                        .filter(Boolean::booleanValue)
                        .next())
                .defaultIfEmpty(false);
    }
}

