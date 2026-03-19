package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products);

    ShoppingCartDto getUsersShoppingCart(String username);

    void deactivateShoppingCart(String username);

    ShoppingCartDto removeProductsFromShoppingCart(String username, List<UUID> products);

    ShoppingCartDto changeProductQuantityInCart(String username, ChangeProductQuantityRequest request);
}