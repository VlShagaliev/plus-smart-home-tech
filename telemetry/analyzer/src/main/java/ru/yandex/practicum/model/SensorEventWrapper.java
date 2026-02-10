package ru.yandex.practicum.model;

import lombok.Data;

@Data
public class SensorEventWrapper {
    private String id;
    private Object data;
}