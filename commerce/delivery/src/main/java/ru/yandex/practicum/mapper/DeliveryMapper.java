package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.model.Delivery;

@Mapper(componentModel = "spring")
public interface DeliveryMapper {

    DeliveryDto mapToDeliveryDto(Delivery delivery);

    @Mapping(target = "toAddress.addressId", ignore = true)
    @Mapping(target = "fromAddress.addressId", ignore = true)
    Delivery mapToDelivery(DeliveryDto dto);
}