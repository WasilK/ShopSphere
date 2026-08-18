package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.cart.CartItemResponse;
import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.*;
import com.wasil.ShopSphere.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public CartService(CartRepository cartRepository,  CartItemRepository cartItemRepository, UserRepository userRepository, ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {

        // 1. Find User
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        ));

        // 2. Find Product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: "
                                        + request.getProductId()
                        ));

        // 3. Find Inventory
        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for product: "
                                        + product.getProdName()
                        ));

        // 4. Find existing Cart or create one
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // 5. Check whether product is already in cart
        Optional<CartItem> existingCartItem =
                cartItemRepository.findByCartAndProduct(cart, product);

        int newQuantity;

        if (existingCartItem.isPresent()) {

            // Product already exists in cart
            CartItem cartItem = existingCartItem.get();

            newQuantity = cartItem.getQuantity() + request.getQuantity();

            // Check total quantity against available stock
            if (newQuantity > inventory.getCurrentStock()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: "
                                + product.getProdName()
                );
            }

            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);

        } else {

            // Product not already in cart
            if (request.getQuantity() > inventory.getCurrentStock()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: "
                                + product.getProdName()
                );
            }

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());

            cartItemRepository.save(cartItem);
        }

        // 6. Return updated cart
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        return convertToCartResponse(cart, cartItems);
    }
    public CartResponse getCartByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        ));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found for user with id: " + userId
                        ));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        return convertToCartResponse(cart, cartItems);
    }
    private CartResponse convertToCartResponse(
            Cart cart,
            List<CartItem> cartItems) {

        CartResponse response = new CartResponse();

        response.setCartId(cart.getCartId());
        response.setUserId(cart.getUser().getUserId());

        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : cartItems) {

            CartItemResponse itemResponse = new CartItemResponse();

            itemResponse.setCartItemId(item.getCartItemId());
            itemResponse.setProductId(item.getProduct().getProdId());
            itemResponse.setProductName(item.getProduct().getProdName());
            itemResponse.setQuantity(item.getQuantity());

            itemResponses.add(itemResponse);
        }

        response.setItems(itemResponses);

        return response;
    }
}
