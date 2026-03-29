package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.PaymentState;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.exceptions.NoPaymentFoundException;
import ru.yandex.practicum.exceptions.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.feign.ShoppingStoreOperations;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.repository.PaymentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreOperations shoppingStoreClient;
    private final OrderOperations orderClient;
    private static final double VAT_RATE = 0.20;

    @Override
    @Transactional
    public PaymentDto createPayment(OrderDto order) {
        validatePaymentInfo(order.getProductPrice(), order.getDeliveryPrice(), order.getTotalPrice());
        Payment payment = paymentMapper.mapToPayment(order);
        payment = paymentRepository.save(payment);
        return paymentMapper.mapToPaymentDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateProductCost(OrderDto order) {
        List<Double> pricesList = new ArrayList<>();
        Map<UUID, Integer> orderProducts = order.getProducts();

        orderProducts.forEach((id, quantity) -> {
            ProductDto product = shoppingStoreClient.getProductById(id);
            double totalProductPrice = product.getPrice() * quantity;
            pricesList.add(totalProductPrice);
        });

        return pricesList.stream().mapToDouble(Double::doubleValue).sum();
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateTotalCost(OrderDto order) {
        validatePaymentInfo(order.getProductPrice(), order.getDeliveryPrice());
        double productsPrice = order.getProductPrice();
        double deliveryPrice = order.getDeliveryPrice();
        return deliveryPrice + productsPrice + (productsPrice * VAT_RATE);
    }

    @Override
    @Transactional
    public void setPaymentSuccessful(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        payment.setPaymentState(PaymentState.SUCCESS);
        orderClient.paymentSuccess(payment.getOrderId());
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void setPaymentFailed(UUID paymentId) {
        Payment payment = getPayment(paymentId);
        payment.setPaymentState(PaymentState.FAILED);
        orderClient.paymentFailed(payment.getOrderId());
        paymentRepository.save(payment);
    }

    private Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId).orElseThrow(() ->
                new NoPaymentFoundException("Payment is not found")
        );
    }

    private void validatePaymentInfo(Double... prices) {
        for (Double price : prices) {
            if (price == null || price == 0) {
                throw new NotEnoughInfoInOrderToCalculateException("Not enough payment info in order");
            }
        }
    }
}