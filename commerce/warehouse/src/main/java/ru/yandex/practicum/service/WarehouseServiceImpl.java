package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.dto.QuantityState;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreOperations;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {
    private static final AddressDto[] ADDRESSES =
            new AddressDto[]{
                    new AddressDto("ADDRESS_1",
                            "ADDRESS_1",
                            "ADDRESS_1",
                            "ADDRESS_1",
                            "ADDRESS_1"),
                    new AddressDto("ADDRESS_2",
                            "ADDRESS_2",
                            "ADDRESS_2",
                            "ADDRESS_2",
                            "ADDRESS_2")};
    private final WarehouseRepository warehouseRepository;
    private final WarehouseProductMapper warehouseProductMapper;
    private final ShoppingStoreOperations shoppingStoreOperations;

    @Override
    @Transactional
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        checkIfProductAlreadyInWarehouse(request.getProductId());
        WarehouseProduct product = warehouseProductMapper.mapToWarehouseProduct(request);
        warehouseRepository.save(product);
    }

    @Override
    @Transactional
    public void increaseProductQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct product = getWarehouseProduct(request.getProductId());
        int quantity = product.getQuantity();
        quantity += request.getQuantity();
        product.setQuantity(quantity);
        warehouseRepository.save(product);
        updateQuantityInShoppingStore(product);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return ADDRESSES[Random.from(new SecureRandom()).nextInt(0, 1)];
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkShoppingCart(ShoppingCartDto shoppingCart) {
        UUID shoppingCartId = shoppingCart.getShoppingCartId();
        Map<UUID, Integer> products = shoppingCart.getProducts();
        Supplier<Stream<WarehouseProduct>> streamSupplier =
                () -> warehouseRepository.findAllById(products.keySet()).stream();
        checkProductQuantity(streamSupplier.get(), products, shoppingCartId);
        return calculateDeliveryParams(streamSupplier);
    }

    private WarehouseProduct getWarehouseProduct(UUID id) {
        return warehouseRepository.findById(id).orElseThrow(() ->
                new NoSpecifiedProductInWarehouseException("Product is not found in warehouse")
        );
    }

    private void checkIfProductAlreadyInWarehouse(UUID id) {
        warehouseRepository.findById(id)
                .ifPresent(product -> {
                    throw new SpecifiedProductAlreadyInWarehouseException("Product is already in warehouse");
                });
    }


    private void checkProductQuantity(Stream<WarehouseProduct> stream, Map<UUID, Integer> products, UUID cartId) {
        if (stream.anyMatch(product -> product.getQuantity() < products.get(product.getProductId()))) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    String.format("Quantity of products is less than necessary for shopping cart ID: %s", cartId)
            );
        }
    }

    private void updateQuantityInShoppingStore(WarehouseProduct product) {
        int quantity = product.getQuantity();
        QuantityState quantityState;

        if (quantity == 0) {
            quantityState = QuantityState.ENDED;
        } else if (0 < quantity && quantity <= 10) {
            quantityState = QuantityState.FEW;
        } else if (10 < quantity && quantity <= 100) {
            quantityState = QuantityState.ENOUGH;
        } else {
            quantityState = QuantityState.MANY;
        }
        try {
            shoppingStoreOperations.updateProductQuantity(
                    new SetProductQuantityStateRequest(product.getProductId(), quantityState));
        } catch (Exception e) {
            // just because we are not adding product to store when add to warehouse updating crushes
        }


    }

    private BookedProductsDto calculateDeliveryParams(Supplier<Stream<WarehouseProduct>> streamSupplier) {
        Double deliveryVolume = streamSupplier.get()
                .map(product -> product.getWidth() * product.getHeight() * product.getDepth())
                .reduce(0.0, Double::sum);

        Double deliveryWeight = streamSupplier.get()
                .map(WarehouseProduct::getWeight)
                .reduce(0.0, Double::sum);

        boolean isFragile = streamSupplier.get().anyMatch(WarehouseProduct::isFragile);
        return new BookedProductsDto(deliveryVolume, deliveryWeight, isFragile);
    }
}