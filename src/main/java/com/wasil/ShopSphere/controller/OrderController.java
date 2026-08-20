package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.dto.order.OrderStatusUpdateRequest;
import com.wasil.ShopSphere.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse createOrder(
            @Valid @RequestBody OrderRequest request) {

        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @PostMapping("/{userId}/checkout")
    public OrderResponse checkoutOrder(@PathVariable Long userId) {
        return orderService.checkoutOrder(userId);
    }
    @PutMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long orderId, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateOrderStatus(orderId, request);
    }
}