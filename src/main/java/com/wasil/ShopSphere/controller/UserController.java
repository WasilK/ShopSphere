package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.dto.user.UserRequest;
import com.wasil.ShopSphere.dto.user.UserResponse;
import com.wasil.ShopSphere.services.CartService;
import com.wasil.ShopSphere.services.OrderService;
import com.wasil.ShopSphere.services.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
     public UserController(UserService userService, OrderService orderService, CartService cartService) {
         this.userService = userService;
         this.orderService = orderService;
         this.cartService = cartService;
     }

     @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id){
        return userService.findUserById(id);
    }

    @PostMapping
    public UserResponse addUser(@Valid @RequestBody UserRequest userRequest){
        return userService.addUser(userRequest);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest){
        return userService.updateUser(id, userRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

    @GetMapping("/{id}/orders")
    public List<OrderResponse> getOrders(@PathVariable Long id){
         return orderService.getOrdersByUser(id);
    }

    @GetMapping("/{id}/cart")
    public CartResponse getCartByUser(@PathVariable Long id){
         return cartService.getCartByUser(id);
    }
}
