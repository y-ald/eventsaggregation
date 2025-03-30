package com.eventsaggregation.service;

import com.eventsaggregation.dto.CountDistinctUsersDto;
import com.eventsaggregation.dto.CountEventsDto;
import com.eventsaggregation.dto.EventExistsDto;
import com.eventsaggregation.repository.ReactiveEventRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class EventService {
    private final ReactiveEventRepository eventRepository;

    public EventService(ReactiveEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Counts the number of events matching the specified criteria.
     *
     * @param from   The start time of the period to search for events.
     * @param to     The end time of the period to search for events.
     * @param event  The event type to count.
     * @param userId The ID of the user whose events to count.
     * @return A Mono emitting the count of events matching the criteria.
     */
    public Mono<CountEventsDto> countEvents(LocalDateTime from, LocalDateTime to, String event, String userId) {
        return eventRepository
                .findEvents(from, to, event, userId)
                .count()
                .map(countEvent -> new CountEventsDto(countEvent));
    }

    /**
     * Counts the number of distinct users who performed a specific event
     * within the given time period.
     *
     * @param from  The start time of the period to search for events.
     * @param to    The end time of the period to search for events.
     * @param event The event type to count distinct users for.
     * @return A Mono emitting the count of distinct users.
     */
    public Mono<CountDistinctUsersDto> countDistinctUsers(LocalDateTime from, LocalDateTime to, String event) {
        return eventRepository
                .findDistinctUsers(from, to, event)
                .count()
                .map(countDistinctUsers -> new CountDistinctUsersDto(countDistinctUsers));
    }

    /**
     * Checks if a specific event exists for a given user.
     *
     * @param userId The ID of the user to check for the event.
     * @param event  The event type to check for.
     * @return A Mono emitting true if the event exists for the user, false otherwise.
     */
    public Mono<EventExistsDto> eventExists(String userId, String event) {
        return eventRepository
                .existsByUserIdAndEvent(userId, event)
                .map(eventExists -> new EventExistsDto(eventExists));
    }
}