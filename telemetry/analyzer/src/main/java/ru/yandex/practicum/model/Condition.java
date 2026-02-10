package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString
@Table(name = "conditions")
@SecondaryTable(name = "scenario_conditions", pkJoinColumns = @PrimaryKeyJoinColumn(name = "condition_id"))
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private ConditionType type;
    private ConditionOperation operation;
    private int value;

    @ManyToOne
    @JoinColumn(name = "scenario_id", table = "scenario_conditions")
    private Scenario scenario;

    @ManyToOne
    @JoinColumn(name = "sensor_id", table = "scenario_conditions")
    private Sensor sensor;
}