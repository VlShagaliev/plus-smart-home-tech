package ru.yandex.practicum.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import ru.yandex.practicum.dto.OrderState;

import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Table(name = "orders")
public class Order {

    @Id
    @UuidGenerator
    UUID orderId;

    String username;

    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    Map<UUID, Integer> products;

    UUID shoppingCartId;

    UUID paymentId;

    UUID deliveryId;

    @Enumerated(EnumType.STRING)
    OrderState state;

    Double deliveryWeight;

    Double deliveryVolume;

    Boolean fragile;

    Double totalPrice;

    Double deliveryPrice;

    Double productPrice;
}