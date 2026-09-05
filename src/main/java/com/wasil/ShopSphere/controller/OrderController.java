package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.dto.order.OrderStatusUpdateRequest;
import com.wasil.ShopSphere.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
    @GetMapping("/me/{id}")
    public OrderResponse getMyOrderById(Authentication authenticated, @PathVariable Long id){
        String email = authenticated.getName();
        return orderService.getMyOrderById(email, id);
    }
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
    @GetMapping("/me")
    public List<OrderResponse> getMyAllOrders(Authentication authenticated){
        String email = authenticated.getName();
        return orderService.getMyAllOrders(email);
    }

    @PutMapping("me/{id}/cancel")
    public OrderResponse cancelOrder(Authentication authenticated, @PathVariable Long id) {
        String email = authenticated.getName();
        return orderService.cancelOrder(email, id);
    }

    @PutMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long orderId, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateOrderStatus(orderId, request);
    }

    @PostMapping("/me/{id}/payment")
    public ResponseEntity<String> paymentSuccess(
            @PathVariable Long id) {

        orderService.handlePaymentSuccess(id);

        return ResponseEntity.ok("Payment successful");
    }
}