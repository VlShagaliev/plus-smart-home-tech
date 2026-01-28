package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.models.HubEvent;
import ru.yandex.practicum.models.SensorEvent;
import ru.yandex.practicum.service.EventService;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class CollectorController {
    private final EventService eventService;

    @PostMapping("/sensors")
    public void sensors(@Valid @RequestBody SensorEvent event) {
        log.info("Request with sensor event: {}", event);
        eventService.publishToSensors(event);
    }

    @PostMapping("/hubs")
    public void hubs(@Valid @RequestBody HubEvent event) {
        log.info("Request with hub event: {}", event);
        eventService.publishToHubs(event);
    }
}
