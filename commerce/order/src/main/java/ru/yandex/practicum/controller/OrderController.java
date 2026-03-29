package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ProductReturnRequest;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/order")
public class OrderController implements OrderOperations {
    private final OrderService orderService;

    @Override
    public List<OrderDto> get(String username) {
        return orderService.getUsersOrders(username);
    }

    @Override
    public OrderDto create(CreateNewOrderRequest newOrder) {
        return orderService.createOrder(newOrder);
    }

    @Override
    public OrderDto returnOrder(ProductReturnRequest productReturnRequest) {
        return orderService.returnOrderProducts(productReturnRequest);
    }

    @Override
    public OrderDto paymentSuccess(UUID orderId) {
        return orderService.setOrderPaid(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        return orderService.setOrderPaymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        return orderService.setOrderDeliverySuccessful(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        return orderService.setOrderDeliveryFailed(orderId);
    }

    @Override
    public OrderDto completed(UUID orderId) {
        return orderService.completeOrder(orderId);
    }

    @Override
    public OrderDto calculateTotal(UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto calculateDelivery(UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        return orderService.setOrderDeliveryInProgress(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        return orderService.setOrderDeliveryAssemblyFailed(orderId);
    }
}