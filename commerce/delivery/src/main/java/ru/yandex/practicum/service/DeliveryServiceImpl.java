package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.DeliveryState;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.exceptions.NoDeliveryFoundException;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.feign.WarehouseOperations;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderOperations orderClient;
    private final WarehouseOperations warehouseClient;

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(5.0);
    private static final double WAREHOUSE_1_ADDRESS_MULTIPLIER = 1;
    private static final double WAREHOUSE_2_ADDRESS_MULTIPLIER = 2;
    private static final double FRAGILE_MULTIPLIER = 0.2;
    private static final double WEIGHT_MULTIPLIER = 0.3;
    private static final double VOLUME_MULTIPLIER = 0.2;
    private static final double STREET_MULTIPLIER = 0.2;

    @Override
    @Transactional
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = deliveryMapper.mapToDelivery(deliveryDto);
        delivery = deliveryRepository.save(delivery);
        return deliveryMapper.mapToDeliveryDto(delivery);
    }

    @Override
    @Transactional
    public void completeDelivery(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        delivery = deliveryRepository.save(delivery);
        orderClient.delivery(delivery.getOrderId());
        deliveryMapper.mapToDeliveryDto(delivery);
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        delivery = deliveryRepository.save(delivery);
        orderClient.deliveryFailed(delivery.getOrderId());
        deliveryMapper.mapToDeliveryDto(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDeliveryCost(OrderDto order) {
        Delivery delivery = getDelivery(order.getDeliveryId());
        Address warehouseAddress = delivery.getFromAddress();
        Address destinationAddress = delivery.getToAddress();

        BigDecimal totalCost = BASE_RATE;

        totalCost.add(warehouseAddress.getCity().equals("ADDRESS_1") ?
                totalCost.multiply(BigDecimal.valueOf(WAREHOUSE_1_ADDRESS_MULTIPLIER)) :
                totalCost.multiply(BigDecimal.valueOf(WAREHOUSE_2_ADDRESS_MULTIPLIER)));

        totalCost.add(Boolean.TRUE.equals(order.getFragile()) ? totalCost.multiply(BigDecimal.valueOf(FRAGILE_MULTIPLIER)) : BigDecimal.valueOf(0));

        totalCost.add(BigDecimal.valueOf(order.getDeliveryWeight()).multiply(BigDecimal.valueOf(WEIGHT_MULTIPLIER)));

        totalCost.add(BigDecimal.valueOf(order.getDeliveryVolume()).multiply(BigDecimal.valueOf(VOLUME_MULTIPLIER)));

        totalCost.add(warehouseAddress.getStreet().equals(destinationAddress.getStreet()) ?
                BigDecimal.valueOf(0) :
                totalCost.multiply(BigDecimal.valueOf(STREET_MULTIPLIER)));

        return totalCost;
    }

    @Override
    public void setDeliveryPicked(UUID deliveryId) {
        Delivery delivery = getDelivery(deliveryId);
        UUID orderId = delivery.getOrderId();
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(orderId, deliveryId));
        orderClient.assembly(orderId);
        delivery = deliveryRepository.save(delivery);
        deliveryMapper.mapToDeliveryDto(delivery);
    }

    private Delivery getDelivery(UUID id) {
        return deliveryRepository.findById(id).orElseThrow(() ->
                new NoDeliveryFoundException("Delivery is not found")
        );
    }
}