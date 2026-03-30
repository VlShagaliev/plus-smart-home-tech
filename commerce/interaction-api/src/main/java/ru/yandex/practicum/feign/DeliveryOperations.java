package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "delivery")
public interface DeliveryOperations {

    @PutMapping
    DeliveryDto planDelivery(@Valid @RequestBody DeliveryDto newDelivery);

    @PostMapping("/successful")
    void successful(@RequestBody UUID deliveryId);

    @PostMapping("/picked")
    void picked(@RequestBody UUID deliveryId);

    @PostMapping("/failed")
    void failed(@RequestBody UUID deliveryId);

    @PostMapping("/cost")
    BigDecimal deliveryCost(@Valid @RequestBody OrderDto orderDto);

}