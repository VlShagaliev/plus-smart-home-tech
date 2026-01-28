package ru.yandex.practicum.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    private String sensorId;
    private ScenarioType type;
    private ScenarioOperation operation;
    private int value;
}
