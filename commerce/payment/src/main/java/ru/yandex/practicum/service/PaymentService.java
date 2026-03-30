package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentDto createPayment(OrderDto order);

    BigDecimal calculateProductCost(OrderDto order);

    BigDecimal calculateTotalCost(OrderDto order);

    void setPaymentSuccessful(UUID paymentId);

    void setPaymentFailed(UUID paymentId);
}