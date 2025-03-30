package com.eventsaggregation.service;

import com.eventsaggregation.dto.CountDistinctUsersDto;
import com.eventsaggregation.dto.CountEventsDto;
import com.eventsaggregation.dto.EventExistsDto;
import com.eventsaggregation.model.Event;
import com.eventsaggregation.repository.ReactiveEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EventServiceTest {

    @Mock
    private ReactiveEventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void countEvents() {
        when(eventRepository.findEvents(any(), any(), any(), any()))
                .thenReturn(Flux.just(new Event("user1", LocalDateTime.now(), "login")));

        StepVerifier.create(eventService.countEvents(LocalDateTime.now(), LocalDateTime.now(), null, null))
                .expectNext(new CountEventsDto(1L))
                .verifyComplete();
    }

    @Test
    void countDistinctUsers() {
        when(eventRepository.findDistinctUsers(any(), any(), any()))
                .thenReturn(Flux.fromIterable(Collections.singletonList("user1")));

        StepVerifier.create(eventService.countDistinctUsers(LocalDateTime.now(), LocalDateTime.now(), null))
                .expectNext(new CountDistinctUsersDto(1L))
                .verifyComplete();
    }

    @Test
    void eventExists() {
        when(eventRepository.existsByUserIdAndEvent("user1", "login"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(eventService.eventExists("user1", "login"))
                .expectNext(new EventExistsDto(true))
                .verifyComplete();
    }
}
