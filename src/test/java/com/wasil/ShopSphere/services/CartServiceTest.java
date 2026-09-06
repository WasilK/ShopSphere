package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.dto.cart.UpdateCartItemRequest;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.*;
import com.wasil.ShopSphere.repositories.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private CartService cartService;


    // =========================================================
    // Helper methods
    // =========================================================

    private User createUser() {

        User user = new User();

        user.setUserId(1L);
        user.setUserEmail("test@gmail.com");
        user.setAddresses(new ArrayList<>());

        return user;
    }


    private Product createProduct() {

        Product product = new Product();

        product.setProdId(1L);
        product.setProdName("iPhone");
        product.setProdIsActive(true);

        return product;
    }


    private Inventory createInventory(Product product) {

        Inventory inventory = new Inventory();

        inventory.setProduct(product);
        inventory.setCurrentStock(10);

        return inventory;
    }


    private Cart createCart(User user) {

        Cart cart = new Cart();

        cart.setCartId(1L);
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());

        return cart;
    }


    private CartItem createCartItem(
            Cart cart,
            Product product,
            int quantity) {

        CartItem cartItem = new CartItem();

        cartItem.setCartItemId(1L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        return cartItem;
    }


    private AddToCartRequest createAddRequest(
            Long productId,
            int quantity) {

        AddToCartRequest request = new AddToCartRequest();

        request.setProductId(productId);
        request.setQuantity(quantity);

        return request;
    }


    private UpdateCartItemRequest createUpdateRequest(
            int quantity) {

        UpdateCartItemRequest request =
                new UpdateCartItemRequest();

        request.setQuantity(quantity);

        return request;
    }


    // =========================================================
    // ADD TO CART
    // =========================================================

    @Test
    void shouldAddNewProductToCartSuccessfully() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();
        Inventory inventory = createInventory(product);
        Cart cart = createCart(user);

        AddToCartRequest request =
                createAddRequest(1L, 2);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.empty());

        when(cartItemRepository.findByCart(cart))
                .thenReturn(new ArrayList<>());

        CartResponse response =
                cartService.addToCart(email, request);

        assertNotNull(response);

        verify(cartItemRepository)
                .save(any(CartItem.class));

        verify(cartItemRepository)
                .findByCart(cart);
    }


    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        String email = "unknown@gmail.com";

        AddToCartRequest request =
                createAddRequest(1L, 2);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> cartService.addToCart(
                                email,
                                request)
                );

        assertTrue(exception.getMessage()
                .contains(email));

        verifyNoInteractions(productRepository);
        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
        verifyNoInteractions(inventoryRepository);
    }


    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        String email = "test@gmail.com";

        User user = createUser();

        AddToCartRequest request =
                createAddRequest(1L, 2);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> cartService.addToCart(
                        email,
                        request)
        );

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenProductIsInactive() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();

        product.setProdIsActive(false);

        AddToCartRequest request =
                createAddRequest(1L, 2);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThrows(
                ProductInActiveException.class,
                () -> cartService.addToCart(
                        email,
                        request)
        );

        verifyNoInteractions(inventoryRepository);
        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();

        AddToCartRequest request =
                createAddRequest(1L, 2);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> cartService.addToCart(
                        email,
                        request)
        );

        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenNewQuantityExceedsStock() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();

        Inventory inventory =
                createInventory(product);

        inventory.setCurrentStock(2);

        Cart cart = createCart(user);

        AddToCartRequest request =
                createAddRequest(1L, 5);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.empty());

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addToCart(
                        email,
                        request)
        );

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }


    @Test
    void shouldIncreaseQuantityWhenProductAlreadyExists() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();
        Inventory inventory = createInventory(product);
        Cart cart = createCart(user);

        CartItem cartItem =
                createCartItem(cart, product, 2);

        AddToCartRequest request =
                createAddRequest(1L, 3);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.of(cartItem));

        when(cartItemRepository.findByCart(cart))
                .thenReturn(List.of(cartItem));

        cartService.addToCart(email, request);

        assertEquals(5, cartItem.getQuantity());

        verify(cartItemRepository)
                .save(cartItem);
    }


    @Test
    void shouldThrowExceptionWhenExistingQuantityExceedsStock() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();

        Inventory inventory =
                createInventory(product);

        inventory.setCurrentStock(4);

        Cart cart = createCart(user);

        CartItem cartItem =
                createCartItem(cart, product, 3);

        AddToCartRequest request =
                createAddRequest(1L, 2);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.of(cartItem));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.addToCart(
                        email, request)
        );

        assertEquals(3, cartItem.getQuantity());

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }


    // =========================================================
    // UPDATE CART ITEM
    // =========================================================

    @Test
    void shouldUpdateCartItemQuantitySuccessfully() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();
        Inventory inventory = createInventory(product);
        Cart cart = createCart(user);

        CartItem cartItem =
                createCartItem(cart, product, 2);

        UpdateCartItemRequest request =
                createUpdateRequest(5);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.of(cartItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(cartItemRepository.findByCart(cart))
                .thenReturn(List.of(cartItem));

        CartResponse response =
                cartService.updateCartItemQuantity(
                        email,
                        1L,
                        request
                );

        assertNotNull(response);
        assertEquals(5, cartItem.getQuantity());

        verify(cartItemRepository)
                .save(cartItem);
    }


    @Test
    void shouldThrowExceptionWhenUpdateUserNotFound() {

        String email = "unknown@gmail.com";

        UpdateCartItemRequest request =
                createUpdateRequest(5);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> cartService.updateCartItemQuantity(
                        email,
                        1L,
                        request)
        );

        verifyNoInteractions(productRepository);
        verifyNoInteractions(cartRepository);
    }


    @Test
    void shouldThrowExceptionWhenUpdateProductNotFound() {

        String email = "test@gmail.com";

        User user = createUser();

        UpdateCartItemRequest request =
                createUpdateRequest(5);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> cartService.updateCartItemQuantity(
                        email,
                        1L,
                        request)
        );

        verifyNoInteractions(cartRepository);
    }


    @Test
    void shouldThrowExceptionWhenCartNotFound() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();

        UpdateCartItemRequest request =
                createUpdateRequest(5);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.updateCartItemQuantity(
                        email,
                        1L,
                        request)
        );

        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenCartItemNotFound() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();
        Cart cart = createCart(user);

        UpdateCartItemRequest request =
                createUpdateRequest(5);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.empty());

        assertThrows(
                CartItemNotFoundException.class,
                () -> cartService.updateCartItemQuantity(
                        email,
                        1L,
                        request)
        );

        verifyNoInteractions(inventoryRepository);
    }


    @Test
    void shouldThrowExceptionWhenUpdateInventoryNotFound() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();
        Cart cart = createCart(user);

        CartItem cartItem =
                createCartItem(cart, product, 2);

        UpdateCartItemRequest request =
                createUpdateRequest(5);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.of(cartItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> cartService.updateCartItemQuantity(
                        email,
                        1L,
                        request)
        );
    }


    @Test
    void shouldThrowExceptionWhenUpdatedQuantityExceedsStock() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();

        Inventory inventory =
                createInventory(product);

        inventory.setCurrentStock(3);

        Cart cart = createCart(user);

        CartItem cartItem =
                createCartItem(cart, product, 1);

        UpdateCartItemRequest request =
                createUpdateRequest(5);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.of(cartItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> cartService.updateCartItemQuantity(
                        email,
                        1L,
                        request)
        );

        assertEquals(1, cartItem.getQuantity());

        verify(cartItemRepository, never())
                .save(any(CartItem.class));
    }


    // =========================================================
    // REMOVE CART ITEM
    // =========================================================

    @Test
    void shouldRemoveCartItemSuccessfully() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();
        Cart cart = createCart(user);

        CartItem cartItem =
                createCartItem(cart, product, 2);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.of(cartItem));

        when(cartItemRepository.findByCart(cart))
                .thenReturn(new ArrayList<>());

        CartResponse response =
                cartService.removeCartItem(
                        email, 1L);

        assertNotNull(response);

        verify(cartItemRepository)
                .delete(cartItem);
    }


    @Test
    void shouldThrowExceptionWhenRemoveUserNotFound() {

        String email = "unknown@gmail.com";

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> cartService.removeCartItem(
                        email, 1L)
        );

        verifyNoInteractions(productRepository);
        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenRemoveProductNotFound() {

        String email = "test@gmail.com";

        User user = createUser();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> cartService.removeCartItem(
                        email, 1L)
        );

        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenRemoveCartNotFound() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.removeCartItem(
                        email, 1L)
        );

        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenRemoveCartItemNotFound() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();
        Cart cart = createCart(user);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartAndProduct(
                cart, product))
                .thenReturn(Optional.empty());

        assertThrows(
                CartItemNotFoundException.class,
                () -> cartService.removeCartItem(
                        email, 1L)
        );

        verify(cartItemRepository, never())
                .delete(any(CartItem.class));
    }


    // =========================================================
    // CLEAR CART
    // =========================================================

    @Test
    void shouldClearCartSuccessfully() {

        String email = "test@gmail.com";

        User user = createUser();
        Cart cart = createCart(user);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCart(cart))
                .thenReturn(new ArrayList<>());

        CartResponse response =
                cartService.clearCart(email);

        assertNotNull(response);

        assertTrue(cart.getCartItems().isEmpty());

        verify(cartRepository)
                .save(cart);

        verify(cartItemRepository)
                .findByCart(cart);
    }


    @Test
    void shouldThrowExceptionWhenClearUserNotFound() {

        String email = "unknown@gmail.com";

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> cartService.clearCart(email)
        );

        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenClearCartNotFound() {

        String email = "test@gmail.com";

        User user = createUser();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.clearCart(email)
        );

        verify(cartRepository)
                .findByUser(user);

        verifyNoInteractions(cartItemRepository);
    }


    // =========================================================
    // GET MY CART
    // =========================================================

    @Test
    void shouldGetMyCartSuccessfully() {

        String email = "test@gmail.com";

        User user = createUser();
        Product product = createProduct();
        Cart cart = createCart(user);

        CartItem cartItem =
                createCartItem(cart, product, 2);

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCart(cart))
                .thenReturn(List.of(cartItem));

        CartResponse response =
                cartService.getMyCart(email);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getCartId()
        );

        assertEquals(
                1L,
                response.getUserId()
        );

        assertNotNull(response.getItems());

        assertEquals(
                1,
                response.getItems().size()
        );

        assertEquals(
                2,
                response.getItems()
                        .get(0)
                        .getQuantity()
        );

        verify(userRepository)
                .findByUserEmail(email);

        verify(cartRepository)
                .findByUser(user);

        verify(cartItemRepository)
                .findByCart(cart);
    }


    @Test
    void shouldThrowExceptionWhenGetCartUserNotFound() {

        String email = "unknown@gmail.com";

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> cartService.getMyCart(email)
        );

        verifyNoInteractions(cartRepository);
        verifyNoInteractions(cartItemRepository);
    }


    @Test
    void shouldThrowExceptionWhenGetCartNotFound() {

        String email = "test@gmail.com";

        User user = createUser();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> cartService.getMyCart(email)
        );

        verifyNoInteractions(cartItemRepository);
    }
}
