package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.feign.PaymentOperations;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController implements PaymentOperations {
    private final PaymentService paymentService;

    @Override
    public PaymentDto payment(OrderDto orderDto) {
        return paymentService.createPayment(orderDto);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        return paymentService.calculateTotalCost(orderDto);
    }

    @Override
    public void refund(UUID paymentId) {
        paymentService.setPaymentSuccessful(paymentId);
    }

    @Override
    public BigDecimal productCost(OrderDto orderDto) {
        return paymentService.calculateProductCost(orderDto);
    }

    @Override
    public void failed(UUID paymentId) {
        paymentService.setPaymentFailed(paymentId);
    }
}