package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.model.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "totalPayment", source = "order.productPrice")
    @Mapping(target = "deliveryTotal", source = "order.deliveryPrice")
    @Mapping(target = "feeTotal", source = "order.totalPrice")
    @Mapping(target = "paymentState", constant = "PENDING")
    Payment mapToPayment(OrderDto order);

    PaymentDto mapToPaymentDto(Payment payment);
}