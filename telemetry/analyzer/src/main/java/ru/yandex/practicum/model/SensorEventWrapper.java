package ru.yandex.practicum.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensorEventWrapper {
    private String id;
    private Object data;
}