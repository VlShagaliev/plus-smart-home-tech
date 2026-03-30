package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.feign.DeliveryOperations;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryOperations {
    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(DeliveryDto newDelivery) {
        return deliveryService.createDelivery(newDelivery);
    }

    @Override
    public void successful(UUID deliveryId) {
        deliveryService.completeDelivery(deliveryId);
    }

    @Override
    public void picked(UUID deliveryId) {
        deliveryService.setDeliveryPicked(deliveryId);
    }

    @Override
    public void failed(UUID deliveryId) {
        deliveryService.deliveryFailed(deliveryId);
    }

    @Override
    public BigDecimal deliveryCost(OrderDto orderDto) {
        return deliveryService.calculateDeliveryCost(orderDto);
    }
}