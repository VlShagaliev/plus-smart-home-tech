package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.CreateNewOrderRequest;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.DeliveryState;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.OrderState;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductReturnRequest;
import ru.yandex.practicum.exceptions.NoOrderFoundException;
import ru.yandex.practicum.exceptions.NotAuthorizedUserException;
import ru.yandex.practicum.feign.DeliveryOperations;
import ru.yandex.practicum.feign.PaymentOperations;
import ru.yandex.practicum.feign.WarehouseOperations;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseOperations warehouseClient;
    private final DeliveryOperations deliveryClient;
    private final PaymentOperations paymentClient;

    @Override
    @Transactional
    public OrderDto createOrder(CreateNewOrderRequest request) {
        BookedProductsDto bookedProducts = warehouseClient.checkShoppingCart(request.getShoppingCart());
        Order order = orderMapper.mapToOrder(request, bookedProducts);
        order = orderRepository.save(order);
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        DeliveryDto newDelivery = new DeliveryDto();
        newDelivery.setFromAddress(warehouseAddress);
        newDelivery.setToAddress(request.getDeliveryAddress());
        newDelivery.setOrderId(order.getOrderId());
        newDelivery.setDeliveryState(DeliveryState.CREATED);
        newDelivery = deliveryClient.planDelivery(newDelivery);
        order.setDeliveryId(newDelivery.getDeliveryId());

        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getUsersOrders(String username) {
        validateUsername(username);
        List<Order> orders = orderRepository.findByUsername(username);
        return orderMapper.mapToListOrderDto(orders);
    }

    @Override
    @Transactional
    public OrderDto returnOrderProducts(ProductReturnRequest request) {
        Order order = getOrder(request.getOrderId());
        warehouseClient.returnItem(request.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);
        orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto setOrderDeliverySuccessful(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.DELIVERED);
        orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto setOrderDeliveryFailed(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto setOrderDeliveryInProgress(UUID orderId) {
        Order order = getOrder(orderId);

        AssemblyProductsForOrderRequest request = new AssemblyProductsForOrderRequest();
        request.setOrderId(order.getOrderId());
        request.setProducts(order.getProducts());
        warehouseClient.assemblyProductForOrderFromShoppingCart(request);

        order.setState(OrderState.ASSEMBLED);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto setOrderDeliveryAssemblyFailed(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto createOrderPayment(UUID orderId) {
        Order order = getOrder(orderId);
        PaymentDto payment = paymentClient.payment(orderMapper.mapToOrderDto(order));
        order.setPaymentId(payment.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto setOrderPaid(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.PAID);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto setOrderPaymentFailed(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto calculateProductCost(UUID orderId) {
        Order order = getOrder(orderId);
        BigDecimal productPrice = paymentClient.productCost(orderMapper.mapToOrderDto(order));
        order.setProductPrice(productPrice);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = getOrder(orderId);
        BigDecimal deliveryPrice = deliveryClient.deliveryCost(orderMapper.mapToOrderDto(order));
        order.setDeliveryPrice(deliveryPrice);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = getOrder(orderId);
        BigDecimal totalPrice = paymentClient.getTotalCost(orderMapper.mapToOrderDto(order));
        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    @Override
    @Transactional
    public OrderDto completeOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.setState(OrderState.COMPLETED);
        order = orderRepository.save(order);
        return orderMapper.mapToOrderDto(order);
    }

    private Order getOrder(UUID id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new NoOrderFoundException("Order is not found")
        );
    }

    private void validateUsername(String username) {
        if (username.isBlank()) {
            throw new NotAuthorizedUserException("Username is blank");
        }
    }
}