package ru.yandex.practicum.service;


import org.springframework.data.domain.Page;
import ru.yandex.practicum.dto.*;

import java.util.Collection;
import java.util.UUID;

public interface ShoppingService {
    ProductDto addProduct(ProductDto product);

    ProductDto findProductById(UUID id);

    ProductDto updateProduct(ProductDto product);

    void removeProductFromStore(UUID productId);

    void setProductQuantityState(UUID productId, QuantityState quantityState);

    Page<ProductDto> searchProducts(String category, int page, int size, String sort);
}