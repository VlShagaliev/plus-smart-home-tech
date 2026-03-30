package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feign.OrderOperations;
import ru.yandex.practicum.feign.ShoppingStoreOperations;
import ru.yandex.practicum.mapper.OrderBookingMapper;
import ru.yandex.practicum.mapper.WarehouseProductMapper;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.OrderBookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.security.SecureRandom;
import java.util.List;
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
    private final OrderOperations orderClient;
    private final OrderBookingRepository orderBookingRepository;
    private final OrderBookingMapper orderBookingMapper;

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
        Map<UUID, Integer> products = shoppingCart.getProducts();
        Supplier<Stream<WarehouseProduct>> streamSupplier =
                () -> warehouseRepository.findAllById(products.keySet()).stream();
        checkProductQuantity(streamSupplier.get(), products);
        return calculateDeliveryParams(streamSupplier);
    }

    @Override
    @Transactional
    public void returnProductsToWarehouse(Map<UUID, Integer> products) {
        List<AddProductToWarehouseRequest> requests = products.entrySet().stream()
                .map(entry -> new AddProductToWarehouseRequest(entry.getKey(), entry.getValue()))
                .toList();
        requests.forEach(this::increaseProductQuantity);
    }

    @Override
    public BookedProductsDto assemblyProducts(AssemblyProductsForOrderRequest request) {
        Map<UUID, Integer> products = request.getProducts();
        Supplier<Stream<WarehouseProduct>> streamSupplier =
                () -> warehouseRepository.findAllById(products.keySet()).stream();

        try {
            checkProductQuantity(streamSupplier.get(), products);
        } catch (ProductInShoppingCartLowQuantityInWarehouse exception) {
            orderClient.assemblyFailed(request.getOrderId());
            throw new ProductInShoppingCartLowQuantityInWarehouse(exception.getMessage());
        }
        orderClient.assembly(request.getOrderId());

        BookedProductsDto bookedProductsParams = calculateDeliveryParams(streamSupplier);
        products.forEach((key, value) -> {
            WarehouseProduct product = getWarehouseProduct(key);
            int oldQuantity = product.getQuantity();
            int decreasingQuantity = value;
            product.setQuantity(oldQuantity - decreasingQuantity);
            warehouseRepository.save(product);
            updateQuantityInShoppingStore(product);
        });
        OrderBooking orderBooking = orderBookingMapper.mapToOrderBooking(bookedProductsParams, request);
        orderBooking = orderBookingRepository.save(orderBooking);
        return orderBookingMapper.mapToBookingDto(orderBooking);
    }

    @Override
    public void shipToDelivery(ShippedToDeliveryRequest request) {
        OrderBooking booking = orderBookingRepository.findByOrderId(request.getOrderId());
        booking.setDeliveryId(request.getDeliveryId());
        orderBookingRepository.save(booking);
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


    private void checkProductQuantity(Stream<WarehouseProduct> stream, Map<UUID, Integer> products) {
        if (stream.anyMatch(product -> product.getQuantity() < products.get(product.getProductId()))) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    String.format("Quantity of products is less than necessary")
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
                    product.getProductId(), quantityState);
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