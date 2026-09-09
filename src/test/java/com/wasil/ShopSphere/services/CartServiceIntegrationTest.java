package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.cart.CartResponse;
import com.wasil.ShopSphere.dto.cart.UpdateCartItemRequest;
import com.wasil.ShopSphere.exceptions.CartItemNotFoundException;
import com.wasil.ShopSphere.exceptions.CartNotFoundException;
import com.wasil.ShopSphere.exceptions.InsufficientStockException;
import com.wasil.ShopSphere.exceptions.InventoryNotFoundException;
import com.wasil.ShopSphere.exceptions.ProductInActiveException;
import com.wasil.ShopSphere.exceptions.ProductNotFoundException;
import com.wasil.ShopSphere.exceptions.UserNotFoundException;
import com.wasil.ShopSphere.model.Cart;
import com.wasil.ShopSphere.model.CartItem;
import com.wasil.ShopSphere.model.Category;
import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.CartItemRepository;
import com.wasil.ShopSphere.repositories.CartRepository;
import com.wasil.ShopSphere.repositories.CategoryRepository;
import com.wasil.ShopSphere.repositories.InventoryRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import com.wasil.ShopSphere.repositories.StockMovementRepository;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CartServiceIntegrationTest {

    @Autowired private CartService cartService;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private StockMovementRepository stockMovementRepository;
    @Autowired private CategoryRepository categoryRepository;

    @BeforeEach
    void cleanDatabase() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        stockMovementRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void addToCart_createsCartAndPersistsItem() {
        User user = createUser();
        Product product = createProduct(true);
        createInventory(product, 10);

        CartResponse response = cartService.addToCart(user.getUserEmail(), addRequest(product.getProdId(), 2));

        assertNotNull(response.getCartId());
        assertEquals(user.getUserId(), response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(product.getProdId(), response.getItems().getFirst().getProductId());
        assertEquals(2, response.getItems().getFirst().getQuantity());

        Cart cart = cartRepository.findByUser(user).orElseThrow();
        assertEquals(2, cartItemRepository.findByCartAndProduct(cart, product).orElseThrow().getQuantity());
    }

    @Test
    void addToCart_increasesExistingItemQuantity() {
        User user = createUser();
        Product product = createProduct(true);
        createInventory(product, 10);
        cartService.addToCart(user.getUserEmail(), addRequest(product.getProdId(), 2));

        CartResponse response = cartService.addToCart(user.getUserEmail(), addRequest(product.getProdId(), 3));

        assertEquals(1, response.getItems().size());
        assertEquals(5, response.getItems().getFirst().getQuantity());
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        assertEquals(1, cartItemRepository.findByCart(cart).size());
    }

    @Test
    void addToCart_rejectsInactiveProductAndInsufficientStock() {
        User user = createUser();
        Product inactive = createProduct(false);
        assertThrows(ProductInActiveException.class,
                () -> cartService.addToCart(user.getUserEmail(), addRequest(inactive.getProdId(), 1)));

        Product product = createProduct(true);
        createInventory(product, 2);
        assertThrows(InsufficientStockException.class,
                () -> cartService.addToCart(user.getUserEmail(), addRequest(product.getProdId(), 3)));

        assertTrue(cartRepository.findByUser(user).isEmpty());
    }

    @Test
    void addToCart_throwsForMissingUserProductOrInventory() {
        assertThrows(UserNotFoundException.class,
                () -> cartService.addToCart("missing@example.com", addRequest(999L, 1)));

        User user = createUser();
        assertThrows(ProductNotFoundException.class,
                () -> cartService.addToCart(user.getUserEmail(), addRequest(999L, 1)));

        Product product = createProduct(true);
        assertThrows(InventoryNotFoundException.class,
                () -> cartService.addToCart(user.getUserEmail(), addRequest(product.getProdId(), 1)));
    }

    @Test
    void updateCartItemQuantity_persistsNewQuantity() {
        User user = createUser();
        Product product = createProduct(true);
        createInventory(product, 10);
        cartService.addToCart(user.getUserEmail(), addRequest(product.getProdId(), 2));

        CartResponse response = cartService.updateCartItemQuantity(user.getUserEmail(), product.getProdId(), updateRequest(7));

        assertEquals(7, response.getItems().getFirst().getQuantity());
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        assertEquals(7, cartItemRepository.findByCartAndProduct(cart, product).orElseThrow().getQuantity());
    }

    @Test
    void updateCartItemQuantity_rejectsQuantityAboveStockWithoutChangingItem() {
        User user = createUser();
        Product product = createProduct(true);
        createInventory(product, 5);
        cartService.addToCart(user.getUserEmail(), addRequest(product.getProdId(), 2));

        assertThrows(InsufficientStockException.class,
                () -> cartService.updateCartItemQuantity(user.getUserEmail(), product.getProdId(), updateRequest(6)));

        Cart cart = cartRepository.findByUser(user).orElseThrow();
        assertEquals(2, cartItemRepository.findByCartAndProduct(cart, product).orElseThrow().getQuantity());
    }

    @Test
    void updateCartItemQuantity_throwsForMissingUserProductCartItemAndInventory() {
        assertThrows(UserNotFoundException.class,
                () -> cartService.updateCartItemQuantity("missing@example.com", 999L, updateRequest(1)));

        User user = createUser();
        assertThrows(ProductNotFoundException.class,
                () -> cartService.updateCartItemQuantity(user.getUserEmail(), 999L, updateRequest(1)));

        Product product = createProduct(true);
        assertThrows(CartNotFoundException.class,
                () -> cartService.updateCartItemQuantity(user.getUserEmail(), product.getProdId(), updateRequest(1)));

        Cart cart = createCart(user);
        assertThrows(CartItemNotFoundException.class,
                () -> cartService.updateCartItemQuantity(user.getUserEmail(), product.getProdId(), updateRequest(1)));

        CartItem item = createCartItem(cart, product, 1);
        assertThrows(InventoryNotFoundException.class,
                () -> cartService.updateCartItemQuantity(user.getUserEmail(), product.getProdId(), updateRequest(2)));
        assertEquals(1, cartItemRepository.findById(item.getCartItemId()).orElseThrow().getQuantity());
    }

    @Test
    void removeCartItem_deletesOnlyRequestedItem() {
        User user = createUser();
        Product first = createProduct(true);
        Product second = createProduct(true);
        createInventory(first, 10);
        createInventory(second, 10);
        cartService.addToCart(user.getUserEmail(), addRequest(first.getProdId(), 1));
        cartService.addToCart(user.getUserEmail(), addRequest(second.getProdId(), 2));

        CartResponse response = cartService.removeCartItem(user.getUserEmail(), first.getProdId());

        assertEquals(1, response.getItems().size());
        assertEquals(second.getProdId(), response.getItems().getFirst().getProductId());
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        assertTrue(cartItemRepository.findByCartAndProduct(cart, first).isEmpty());
    }

    @Test
    void removeCartItem_throwsForMissingUserProductCartOrItem() {
        assertThrows(UserNotFoundException.class, () -> cartService.removeCartItem("missing@example.com", 999L));

        User user = createUser();
        assertThrows(ProductNotFoundException.class, () -> cartService.removeCartItem(user.getUserEmail(), 999L));

        Product product = createProduct(true);
        assertThrows(CartNotFoundException.class,
                () -> cartService.removeCartItem(user.getUserEmail(), product.getProdId()));

        createCart(user);
        assertThrows(CartItemNotFoundException.class,
                () -> cartService.removeCartItem(user.getUserEmail(), product.getProdId()));
    }

    @Test
    void clearCart_removesEveryPersistedItemButKeepsCart() {
        User user = createUser();
        Product first = createProduct(true);
        Product second = createProduct(true);
        createInventory(first, 10);
        createInventory(second, 10);
        cartService.addToCart(user.getUserEmail(), addRequest(first.getProdId(), 1));
        cartService.addToCart(user.getUserEmail(), addRequest(second.getProdId(), 2));

        CartResponse response = cartService.clearCart(user.getUserEmail());

        assertTrue(response.getItems().isEmpty());
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        assertTrue(cartItemRepository.findByCart(cart).isEmpty());
        assertEquals(1, cartRepository.count());
    }

    @Test
    void clearCartAndGetMyCart_throwForMissingUserOrCart() {
        assertThrows(UserNotFoundException.class, () -> cartService.clearCart("missing@example.com"));
        assertThrows(UserNotFoundException.class, () -> cartService.getMyCart("missing@example.com"));

        User user = createUser();
        assertThrows(CartNotFoundException.class, () -> cartService.clearCart(user.getUserEmail()));
        assertThrows(CartNotFoundException.class, () -> cartService.getMyCart(user.getUserEmail()));
    }

    @Test
    void getMyCart_returnsPersistedCartContents() {
        User user = createUser();
        Product product = createProduct(true);
        createInventory(product, 10);
        cartService.addToCart(user.getUserEmail(), addRequest(product.getProdId(), 4));

        CartResponse response = cartService.getMyCart(user.getUserEmail());

        assertEquals(user.getUserId(), response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(product.getProdId(), response.getItems().getFirst().getProductId());
        assertEquals(4, response.getItems().getFirst().getQuantity());
    }

    private User createUser() {
        User user = new User();
        user.setFirstName("Cart");
        user.setLastName("Tester");
        user.setUserEmail("cart-test@example.com");
        user.setUserPassword("password");
        user.setUserPhone("9999999999");
        return userRepository.save(user);
    }

    private Product createProduct(boolean active) {
        Category category = new Category();
        category.setCategoryName("Cart category " + categoryRepository.count());
        category.setCategoryIsActive(true);
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setProdName("Cart product " + productRepository.count());
        product.setProdPrice(new BigDecimal("499.99"));
        product.setProdDescription("A product used only by CartService integration tests");
        product.setProdIsActive(active);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private Inventory createInventory(Product product, int stock) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setCurrentStock(stock);
        return inventoryRepository.save(inventory);
    }

    private Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private CartItem createCartItem(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    private AddToCartRequest addRequest(Long productId, int quantity) {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    private UpdateCartItemRequest updateRequest(int quantity) {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(quantity);
        return request;
    }
}
