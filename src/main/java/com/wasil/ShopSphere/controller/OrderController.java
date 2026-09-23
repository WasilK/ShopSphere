package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.dto.order.OrderStatusUpdateRequest;
import com.wasil.ShopSphere.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Order API's")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    @Operation(summary = "Gets an order using order id, only allowed for admin.")
    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
    @Operation(summary = "Gets an order of the logged in user, using order id")
    @GetMapping("/me/{id}")
    public OrderResponse getMyOrderById(Authentication authenticated, @PathVariable Long id){
        String email = authenticated.getName();
        return orderService.getMyOrderById(email, id);
    }
    @Operation(summary = "Gets all the orders, only allowed for admin.")
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
    @Operation(summary = "Gets all the orders of the logged in user.")
    @GetMapping("/me")
    public List<OrderResponse> getMyAllOrders(Authentication authenticated){
        String email = authenticated.getName();
        return orderService.getMyAllOrders(email);
    }
    @Operation(summary = "Cancels the order of the logged in user.")
    @PutMapping("me/{id}/cancel")
    public OrderResponse cancelOrder(Authentication authenticated, @PathVariable Long id) {
        String email = authenticated.getName();
        return orderService.cancelOrder(email, id);
    }
    @Operation(summary = "Updates the order status using order id, only allowed for admin.")
    @PutMapping("/{orderId}/status")
    public OrderResponse updateOrderStatus(@PathVariable Long orderId, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateOrderStatus(orderId, request);
    }
    @Operation(summary = "Admin cancel's the order from his end.")
    @PutMapping("{id}/cancel")
    public OrderResponse adminCancelOrder(@PathVariable Long id){
        return orderService.adminCancelOrder(id);
    }

    // NOTE: order confirmation used to be a separate, unauthenticated-ownership
    // endpoint here that any logged-in user could call for any order ID.
    // It has been removed — PaymentService.processPayment() now calls
    // orderService.handlePaymentSuccess(email, orderId) directly, in the
    // same transaction as the payment write, with an ownership check.
}