package com.eventsaggregation.controller;

import com.eventsaggregation.dto.CountDistinctUsersDto;
import com.eventsaggregation.dto.CountEventsDto;
import com.eventsaggregation.dto.EventExistsDto;
import com.eventsaggregation.model.Event;
import com.eventsaggregation.repository.ReactiveEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
@AutoConfigureWebTestClient
class EventControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveEventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.save(new Event("user1", LocalDateTime.now(), "login")).block();
    }

    @AfterEach
    void cleanUp() {
        eventRepository.deleteALl();
    }

    @Test
    void countEvents() {
        String from = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String to = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/count")
                        .queryParam("date_from", from)
                        .queryParam("date_to", to)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CountEventsDto.class)
                .isEqualTo(new CountEventsDto(1L));
    }

    @Test
    void countDistinctUsers() {
        String from = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String to = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/count_distinct_users")
                        .queryParam("date_from", from)
                        .queryParam("date_to", to)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CountDistinctUsersDto.class)
                .isEqualTo(new CountDistinctUsersDto(1L));
    }

    @Test
    void eventExists() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/exists")
                        .queryParam("event", "login")
                        .queryParam("user_id", "user1")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(EventExistsDto.class)
                .isEqualTo(new EventExistsDto(true));
    }
}
