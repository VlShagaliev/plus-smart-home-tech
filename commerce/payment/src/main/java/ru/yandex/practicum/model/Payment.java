package ru.yandex.practicum.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import ru.yandex.practicum.dto.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment {

    @Id
    @UuidGenerator
    private UUID paymentId;

    private UUID orderId;

    private BigDecimal totalPayment;

    private double deliveryTotal;

    private double feeTotal;

    @Enumerated(EnumType.STRING)
    private PaymentState paymentState;

}