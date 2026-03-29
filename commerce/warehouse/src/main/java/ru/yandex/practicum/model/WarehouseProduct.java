package ru.yandex.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "warehouse_product")
public class WarehouseProduct {

    @Id
    private UUID productId;

    private double weight;

    private double width;

    private double height;

    private double depth;

    private boolean fragile;

    private int quantity;
}