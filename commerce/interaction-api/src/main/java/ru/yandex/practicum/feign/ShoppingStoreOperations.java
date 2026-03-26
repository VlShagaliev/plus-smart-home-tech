package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;

import java.util.Collection;
import java.util.UUID;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreOperations {

    @PutMapping
    ProductDto addProduct(@Valid @RequestBody ProductDto product);

    @GetMapping
    Collection<ProductDto> searchProducts(@RequestParam("category") ProductCategory category,
                                          @RequestParam(required = false) Pageable params);

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @PostMapping
    ProductDto updateProduct(@Valid @RequestBody ProductDto product);

    @PostMapping("/removeProductFromStore")
    boolean removeProduct(@RequestBody UUID productId);


    @PostMapping("/quantityState")
    @ResponseStatus(HttpStatus.OK)
    boolean updateProductQuantity(@NotNull @RequestParam("productId") UUID productId,
                                  @RequestParam QuantityState quantityState);
}