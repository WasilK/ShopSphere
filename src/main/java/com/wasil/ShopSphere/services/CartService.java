package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.cart.CartItemResponse;
import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.dto.cart.UpdateCartItemRequest;
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
    public CartResponse addToCart(String email, AddToCartRequest request) {
        // 1. Find User
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        ));
        // 2. Find Product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: "
                                        + request.getProductId()
                        ));
        if(!product.getProdIsActive()){
            throw new ProductInActiveException("Product is inactive it cannot be added to cart.");
        }
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
    @Transactional
    public CartResponse updateCartItemQuantity(
            String email,
            Long prodId,
            UpdateCartItemRequest request) {
        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + email
                        ));
        Product product = productRepository.findById(prodId).orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + prodId));
        // 1. Find CartItem
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new CartNotFoundException("Cart not found for this user " + email));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new CartItemNotFoundException(
                                "Cart item not found with id: " + prodId
                        ));

        // 4. Get Inventory
        Inventory inventory = inventoryRepository.findByProduct(product)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for product: "
                                        + product.getProdName()
                        ));

        // 5. Check requested quantity against available stock
        if (request.getQuantity() > inventory.getCurrentStock()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: "
                            + product.getProdName()
            );
        }

        // 6. Update quantity
        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);

        // 7. Get updated cart items
        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        // 8. Return updated cart
        return convertToCartResponse(cart, cartItems);
    }
    @Transactional
    public CartResponse removeCartItem(String email, Long prodId){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email."));

        Product product = productRepository.findById(prodId).orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + prodId));
        // 1. Find CartItem
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new CartNotFoundException("Cart not found for this user " + email));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() ->
                        new CartItemNotFoundException(
                                "Cart item not found with id: " + prodId
                        ));
        cartItemRepository.delete(cartItem);
        return convertToCartResponse(cart, cartItemRepository.findByCart(cart));
    }
    @Transactional
    public CartResponse clearCart(String email) {

        User user = userRepository.findByUserEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        ));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found for user with email: " + email
                        ));

        // CartItem is the owning side of this relationship. Fetch and delete
        // persisted items explicitly so this also works when the Cart entity's
        // inverse collection has not been loaded or synchronized.
        cartItemRepository.deleteAllInBatch(cartItemRepository.findByCart(cart));

        return convertToCartResponse(cart, cartItemRepository.findByCart(cart));
    }
    @Transactional
    public CartResponse getMyCart(String email){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email."));
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new CartNotFoundException("Cart not found for this user " + email));
        return convertToCartResponse(cart, cartItemRepository.findByCart(cart));
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
