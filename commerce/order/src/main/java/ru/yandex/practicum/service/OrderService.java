package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(CreateNewOrderRequest request);

    List<OrderDto> getUsersOrders(String username);

    OrderDto returnOrderProducts(ProductReturnRequest request);

    OrderDto setOrderDeliverySuccessful(UUID orderId);

    OrderDto setOrderDeliveryFailed(UUID orderId);

    OrderDto setOrderDeliveryInProgress(UUID orderId);

    OrderDto setOrderDeliveryAssemblyFailed(UUID orderId);

    OrderDto createOrderPayment(UUID orderId);

    OrderDto setOrderPaid(UUID orderId);

    OrderDto setOrderPaymentFailed(UUID orderId);

    OrderDto calculateProductCost(UUID orderId);

    OrderDto calculateDeliveryCost(UUID orderId);

    OrderDto calculateTotalCost(UUID orderId);

    OrderDto completeOrder(UUID orderId);
}