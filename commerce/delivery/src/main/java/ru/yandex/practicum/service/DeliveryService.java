package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {

    DeliveryDto createDelivery(DeliveryDto delivery);

    void completeDelivery(UUID deliveryId);

    void deliveryFailed(UUID deliveryId);

    BigDecimal calculateDeliveryCost(OrderDto order);

    void setDeliveryPicked(UUID deliveryId);
}