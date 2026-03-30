package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.model.OrderBooking;

@Mapper(componentModel = "spring")
public interface OrderBookingMapper {
    @Mapping(target = "fragile", source = "productsParams.fragile")
    @Mapping(target = "deliveryWeight", source = "productsParams.deliveryWeight")
    @Mapping(target = "deliveryVolume", source = "productsParams.deliveryVolume")
    @Mapping(target = "products", source = "request.products")
    @Mapping(target = "orderId", source = "request.orderId")
    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "deliveryId", ignore = true)
    OrderBooking mapToOrderBooking(BookedProductsDto productsParams, AssemblyProductsForOrderRequest request);

    BookedProductsDto mapToBookingDto(OrderBooking booking);
}