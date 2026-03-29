package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    @NotNull
    UUID paymentId;

    @NotNull
    UUID orderId;

    @NotNull
    Double totalPayment;

    @NotNull
    Double deliveryTotal;

    @NotNull
    Double feeTotal;

    PaymentState paymentState;
}