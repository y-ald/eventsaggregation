package com.eventsaggregation.repository;

import com.eventsaggregation.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

class ReactiveEventRepositoryTest {

    private ReactiveEventRepository repository;
    private Event event1;
    private Event event2;

    @BeforeEach
    void setUp() {
        repository = new ReactiveEventRepository();
        event1 = new Event("user1", LocalDateTime.now(), "login");
        event2 = new Event("user2", LocalDateTime.now(), "logout");
        repository.save(event1).block();
        repository.save(event2).block();
    }

    @Test
    void saveAndFindEvents() {

        Flux<Event> events = repository.findEvents(event1.timestamp(), event1.timestamp(), null, "user1");

        StepVerifier.create(events)
                .expectNext(event1)
                .verifyComplete();
    }

    @Test
    void findDistinctUsers() {


        Flux<String> distinctUsers = repository.findDistinctUsers(event1.timestamp().minusMinutes(2), event1.timestamp().plusMinutes(2), null);

        StepVerifier.create(distinctUsers)
                .expectNext("user1", "user2")
                .verifyComplete();
    }

    @Test
    void existsByUserIdAndEvent() {
        Event event1 = new Event("user1", LocalDateTime.now(), "login");
        Event event2 = new Event("user2", LocalDateTime.now(), "logout");
        repository.save(event1).block();
        repository.save(event2).block();

        Mono<Boolean> exists = repository.existsByUserIdAndEvent("user1", "login");

        StepVerifier.create(exists)
                .expectNext(true)
                .verifyComplete();
    }
}
