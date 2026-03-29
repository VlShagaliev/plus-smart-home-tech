package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.model.Order;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "shoppingCartId", source = "request.shoppingCart.shoppingCartId")
    @Mapping(target = "products", source = "request.shoppingCart.products")
    @Mapping(target = "state", constant = "NEW")
    @Mapping(target = "deliveryWeight", source = "bookedProducts.deliveryWeight")
    @Mapping(target = "deliveryVolume", source = "bookedProducts.deliveryVolume")
    @Mapping(target = "fragile", source = "bookedProducts.fragile")
    Order mapToOrder(CreateNewOrderRequest request, BookedProductsDto bookedProducts);

    OrderDto mapToOrderDto(Order order);

    List<OrderDto> mapToListOrderDto(List<Order> orders);
}