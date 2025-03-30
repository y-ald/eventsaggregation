package com.eventsaggregation.service;

import com.eventsaggregation.model.Event;
import com.eventsaggregation.repository.ReactiveEventRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EventLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(EventLoaderService.class);
    public static final String DATA_FILE_PATH = "/Users/pauhappy/data";
    private final ReactiveEventRepository eventRepository;

    public EventLoaderService(ReactiveEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Method executed after the bean's initialization.
     * It loads events from files in the specified data directory.
     *
     * @throws IOException if an I/O error occurs during file reading.
     */
    @PostConstruct
    public void loadEventsFromFiles() throws IOException {
        logger.info("Start loading events from files");
        File folder = new File(DATA_FILE_PATH);

        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                logger.info("Processing file: " + file.getName());
                processFile(file);
            }
        } else {
            logger.info("Data folder not found.");
        }
    }

    /**
     * Processes a single file to extract events and save them to the repository.
     *
     * @param file The file to be processed.
     * @throws IOException if an I/O error occurs during file reading.
     */
    private void processFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8));

        Flux.fromStream(reader.lines())
                .publishOn(Schedulers.boundedElastic())
                .map(line -> {
                    String[] parts = line.split("\t");
                    String userId = parts[0];
                    LocalDateTime timestamp = LocalDateTime.parse(parts[1], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    String event = parts[2];
                    return new Event(userId, timestamp, event);
                })
                .flatMap(eventRepository::save)
                .then()
                .subscribe();
    }
}

