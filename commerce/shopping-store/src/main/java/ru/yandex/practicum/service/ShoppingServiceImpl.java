package ru.yandex.practicum.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exceptions.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ShoppingStoreRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ShoppingServiceImpl implements ShoppingService {
    private final ShoppingStoreRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto addProduct(ProductDto product) {
        Product productDb = productMapper.mapToProduct(product);
        productDb = productRepository.save(productDb);
        return productMapper.mapToProductDto(productDb);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto findProductById(UUID id) {
        Product product = getProductFromStore(id);
        return productMapper.mapToProductDto(product);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto product) {
        getProductFromStore(product.getProductId());
        Product productUpdated = productMapper.mapToProduct(product);
        productUpdated = productRepository.save(productUpdated);
        return productMapper.mapToProductDto(productUpdated);
    }

    @Override
    @Transactional
    public void removeProductFromStore(UUID productId) {
        Product product = getProductFromStore(productId);
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void setProductQuantityState(UUID productId, QuantityState quantityState) {
        Product product = getProductFromStore(productId);
        product.setQuantityState(quantityState);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String category, int page, int size, String sortIn) {
        Sort sort;
        if (sortIn != null && !sortIn.trim().isEmpty()) {
            List<Sort.Order> sortOrders = parseSortCriteria(sortIn);
            sort = Sort.by(sortOrders);
        } else {
            sort = Sort.unsorted();
        }
        PageRequest pageable = PageRequest.of(page, size, sort);
        ProductCategory productCategory;
        try {
            productCategory = ProductCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid product category: " + category);
        }
        return productRepository.findAllByProductCategory(productCategory, pageable)
                .map(ProductMapper.INSTANCE::mapToProductDto);
    }

    private Product getProductFromStore(UUID productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Product is not found"));
    }

    private List<Sort.Order> parseSortCriteria(String sortCriteria) {
        String[] parts = sortCriteria.split(",");
        List<Sort.Order> orders = new ArrayList<>();

        for (int i = 0; i < parts.length; i += 2) {
            String field = parts[i].trim();
            String direction = parts[i + 1].trim().toUpperCase();

            if (field.isEmpty()) {
                throw new IllegalArgumentException("Sort field cannot be empty");
            }

            Sort.Order order;
            switch (direction) {
                case "ASC":
                    order = Sort.Order.asc(field);
                    break;
                case "DESC":
                    order = Sort.Order.desc(field);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid sort direction: " + direction + ". Use 'ASC' or 'DESC'"
                    );
            }
            orders.add(order);
        }

        return orders;
    }
}