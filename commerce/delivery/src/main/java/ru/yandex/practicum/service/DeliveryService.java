package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;

import java.util.UUID;

public interface DeliveryService {

    DeliveryDto createDelivery(DeliveryDto delivery);

    void completeDelivery(UUID deliveryId);

    void deliveryFailed(UUID deliveryId);

    Double calculateDeliveryCost(OrderDto order);

    void setDeliveryPicked(UUID deliveryId);
}