package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private UUID productId;

    @NotNull
    @Size(min = 1)
    private String productName;

    @NotNull
    @Size(min = 1)
    private String description;

    private String imageSrc;

    private QuantityState quantityState;

    private ProductState productState;

    private Double rating;

    private ProductCategory productCategory;

    @NotNull
    private Double price;
}