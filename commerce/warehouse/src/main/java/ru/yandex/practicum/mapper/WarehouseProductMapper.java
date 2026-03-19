package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.WarehouseProduct;

@Mapper(componentModel = "spring")
public interface WarehouseProductMapper {
    @Mapping(target = "width", source = "request.dimension.width")
    @Mapping(target = "height", source = "request.dimension.height")
    @Mapping(target = "depth", source = "request.dimension.depth")
    @Mapping(target = "quantity", constant = "1000")
    WarehouseProduct mapToWarehouseProduct(NewProductInWarehouseRequest request);
}