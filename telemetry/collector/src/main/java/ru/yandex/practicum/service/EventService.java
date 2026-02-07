package ru.yandex.practicum.service;

import ru.yandex.practicum.models.HubEvent;
import ru.yandex.practicum.models.SensorEvent;

public interface EventService {
    void publishToSensors(SensorEvent event);

    void publishToHubs(HubEvent event);
}
