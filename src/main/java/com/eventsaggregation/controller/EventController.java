package com.eventsaggregation.controller;

import com.eventsaggregation.dto.CountDistinctUsersDto;
import com.eventsaggregation.dto.CountEventsDto;
import com.eventsaggregation.dto.EventExistsDto;
import com.eventsaggregation.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;


@RestController
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Endpoint to count the number of events based on the provided parameters.
     *
     * @param date_from The start date and time of the period to search for events.
     * @param date_to   The end date and time of the period to search for events.
     * @param event     (Optional) The event type to filter by.
     * @param user_id   (Optional) The user ID to filter by.
     * @return A Mono emitting the count of events matching the criteria.
     */
    @GetMapping("/count")
    public Mono<CountEventsDto> countEvents(@RequestParam String date_from,
                                            @RequestParam String date_to,
                                            @RequestParam(required = false) String event,
                                            @RequestParam(required = false) String user_id) {
        logger.info("Count total events query with params, date_from: {} date_to: {} event: {} user_id: {}", date_from, date_to, event, user_id);
        LocalDateTime from = LocalDateTime.parse(date_from);
        LocalDateTime to = LocalDateTime.parse(date_to);
        return eventService.countEvents(from, to, event, user_id);
    }

    /**
     * Endpoint to count the number of distinct users who performed a specific event within the given time period.
     *
     * @param date_from The start date and time of the period to search for events.
     * @param date_to   The end date and time of the period to search for events.
     * @param event     (Optional) The event type to filter by.
     * @return A Mono emitting the count of distinct users who match the criteria.
     */
    @GetMapping("/count_distinct_users")
    public Mono<CountDistinctUsersDto> countDistinctUsers(@RequestParam String date_from,
                                                          @RequestParam String date_to,
                                                          @RequestParam(required = false) String event) {
        logger.info("Count distinct events query with params, date_from: {} date_to: {} event: {}", date_from, date_to, event);
        LocalDateTime from = LocalDateTime.parse(date_from);
        LocalDateTime to = LocalDateTime.parse(date_to);
        return eventService.countDistinctUsers(from, to, event);
    }

    /**
     * Endpoint to check if a specific event exists for a given user.
     *
     * @param event   The event type to check for.
     * @param user_id The ID of the user to check for the event.
     * @return A Mono emitting true if the event exists for the user, false otherwise.
     */
    @GetMapping("/exists")
    public Mono<EventExistsDto> eventExists(@RequestParam String event,
                                            @RequestParam String user_id) {
        logger.info("Check if event exists query with params, event: {} user_id: {}", event, user_id);
        return eventService.eventExists(user_id, event);
    }
}
