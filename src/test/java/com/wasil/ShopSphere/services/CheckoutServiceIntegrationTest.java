package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.cart.AddToCartRequest;
import com.wasil.ShopSphere.dto.order.OrderItemRequest;
import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.exceptions.CartEmptyException;
import com.wasil.ShopSphere.exceptions.InsufficientStockException;
import com.wasil.ShopSphere.exceptions.UserNotFoundException;
import com.wasil.ShopSphere.model.Address;
import com.wasil.ShopSphere.model.AddressType;
import com.wasil.ShopSphere.model.Cart;
import com.wasil.ShopSphere.model.Category;
import com.wasil.ShopSphere.model.IdempotencyKey;
import com.wasil.ShopSphere.model.IdempotencyStatus;
import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.Order;
import com.wasil.ShopSphere.model.OrderItem;
import com.wasil.ShopSphere.model.OrderStatus;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.AddressRepository;
import com.wasil.ShopSphere.repositories.CartItemRepository;
import com.wasil.ShopSphere.repositories.CartRepository;
import com.wasil.ShopSphere.repositories.CategoryRepository;
import com.wasil.ShopSphere.repositories.IdempotencyRepository;
import com.wasil.ShopSphere.repositories.InventoryRepository;
import com.wasil.ShopSphere.repositories.OrderItemRepository;
import com.wasil.ShopSphere.repositories.OrderRepository;
import com.wasil.ShopSphere.repositories.PaymentRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import com.wasil.ShopSphere.repositories.StockMovementRepository;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CheckoutServiceIntegrationTest {

    private static final String EMAIL = "checkout-test@example.com";

    @Autowired private CheckoutService checkoutService;
    @Autowired private CartService cartService;
    @Autowired private IdempotencyRepository idempotencyRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private StockMovementRepository stockMovementRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
        idempotencyRepository.deleteAll();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        stockMovementRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createCheckoutOrder_createsPendingOrderClearsCartAndCompletesIdempotencyKey() {
        User user = createUserWithAddress();
        Product product = createProduct("Phone", "500.00");
        createInventory(product, 10);
        addToCart(user, product, 2);

        OrderResponse response = checkoutService.createCheckoutOrder(user.getUserEmail(), "cart-checkout-1");

        assertNotNull(response.getOrderId());
        assertEquals(user.getUserId(), response.getUserId());
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertEquals(0, new BigDecimal("1000.00").compareTo(response.getTotalAmount()));
        assertEquals(1, response.getOrderItems().size());
        assertEquals(product.getProdId(), response.getOrderItems().getFirst().getProductId());
        assertEquals(2, response.getOrderItems().getFirst().getQuantity());
        assertEquals(0, new BigDecimal("500.00").compareTo(response.getOrderItems().getFirst().getPrice()));

        Order order = orderRepository.findById(response.getOrderId()).orElseThrow();
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        assertEquals(1, items.size());
        assertEquals(2, items.getFirst().getQuantity());

        Cart cart = cartRepository.findByUser(user).orElseThrow();
        assertTrue(cartItemRepository.findByCart(cart).isEmpty());

        IdempotencyKey key = idempotencyRepository
                .findByUser_UserEmailAndIdempotencyKey(user.getUserEmail(), "cart-checkout-1")
                .orElseThrow();
        assertEquals(IdempotencyStatus.COMPLETED, key.getStatus());
        assertEquals(order.getOrderId(), key.getOrderId());
    }

    @Test
    void createCheckoutOrder_reusesCompletedIdempotencyKeyWithoutCreatingAnotherOrder() {
        User user = createUserWithAddress();
        Product product = createProduct("Phone", "500.00");
        createInventory(product, 10);
        addToCart(user, product, 1);

        OrderResponse first = checkoutService.createCheckoutOrder(user.getUserEmail(), "cart-repeat-1");
        OrderResponse repeated = checkoutService.createCheckoutOrder(user.getUserEmail(), "cart-repeat-1");

        assertEquals(first.getOrderId(), repeated.getOrderId());
        assertEquals(1, orderRepository.count());
        assertEquals(1, idempotencyRepository.count());
    }

    @Test
    void createCheckoutOrder_rollsBackOrderAndKeyWhenCartIsEmpty() {
        User user = createUserWithAddress();
        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);

        assertThrows(CartEmptyException.class,
                () -> checkoutService.createCheckoutOrder(user.getUserEmail(), "empty-cart-1"));

        assertEquals(0, orderRepository.count());
        assertTrue(idempotencyRepository
                .findByUser_UserEmailAndIdempotencyKey(user.getUserEmail(), "empty-cart-1")
                .isEmpty());
    }

    @Test
    void directCheckoutOrder_createsOrderAndCompletesIdempotencyKey() {
        User user = createUserWithAddress();
        Product product = createProduct("Laptop", "1200.00");
        createInventory(product, 5);

        OrderResponse response = checkoutService.directCheckoutOrder(
                user.getUserEmail(), "direct-checkout-1", orderRequest(product.getProdId(), 3));

        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertEquals(0, new BigDecimal("3600.00").compareTo(response.getTotalAmount()));
        assertEquals(1, response.getOrderItems().size());
        assertEquals(3, response.getOrderItems().getFirst().getQuantity());
        assertEquals(1, orderRepository.count());

        IdempotencyKey key = idempotencyRepository
                .findByUser_UserEmailAndIdempotencyKey(user.getUserEmail(), "direct-checkout-1")
                .orElseThrow();
        assertEquals(IdempotencyStatus.COMPLETED, key.getStatus());
        assertEquals(response.getOrderId(), key.getOrderId());
    }

    @Test
    void directCheckoutOrder_reusesCompletedKeyWithoutCreatingAnotherOrder() {
        User user = createUserWithAddress();
        Product product = createProduct("Laptop", "1200.00");
        createInventory(product, 5);
        OrderRequest request = orderRequest(product.getProdId(), 1);

        OrderResponse first = checkoutService.directCheckoutOrder(user.getUserEmail(), "direct-repeat-1", request);
        OrderResponse repeated = checkoutService.directCheckoutOrder(user.getUserEmail(), "direct-repeat-1", request);

        assertEquals(first.getOrderId(), repeated.getOrderId());
        assertEquals(1, orderRepository.count());
        assertEquals(1, idempotencyRepository.count());
    }

    @Test
    void directCheckoutOrder_rollsBackWhenStockIsInsufficient() {
        User user = createUserWithAddress();
        Product product = createProduct("Laptop", "1200.00");
        createInventory(product, 2);

        assertThrows(InsufficientStockException.class,
                () -> checkoutService.directCheckoutOrder(
                        user.getUserEmail(), "insufficient-stock-1", orderRequest(product.getProdId(), 3)));

        assertEquals(0, orderRepository.count());
        assertTrue(idempotencyRepository
                .findByUser_UserEmailAndIdempotencyKey(user.getUserEmail(), "insufficient-stock-1")
                .isEmpty());
    }

    @Test
    void directCheckoutOrder_throwsWhenUserDoesNotExist() {
        assertThrows(UserNotFoundException.class,
                () -> checkoutService.directCheckoutOrder(
                        "missing@example.com", "missing-user-1", orderRequest(999L, 1)));
        assertEquals(0, orderRepository.count());
        assertEquals(0, idempotencyRepository.count());
    }

    private User createUserWithAddress() {
        User user = new User();
        user.setFirstName("Checkout");
        user.setLastName("Tester");
        user.setUserEmail(EMAIL);
        user.setUserPassword("password");
        user.setUserPhone("9999999999");
        user = userRepository.save(user);

        Address address = new Address();
        address.setStreet("1 Test Street");
        address.setArea("Test Area");
        address.setCity("Test City");
        address.setState("Test State");
        address.setZip("123456");
        address.setAddressType(AddressType.HOME);
        address.setDefaultAddress(true);
        address.setUser(user);
        addressRepository.save(address);
        return user;
    }

    private Product createProduct(String name, String price) {
        Category category = new Category();
        category.setCategoryName("Checkout Category " + categoryRepository.count());
        category.setCategoryIsActive(true);
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setProdName(name);
        product.setProdPrice(new BigDecimal(price));
        product.setProdDescription("Product used by CheckoutService integration tests");
        product.setProdIsActive(true);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private void createInventory(Product product, int stock) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setCurrentStock(stock);
        inventoryRepository.save(inventory);
    }

    private void addToCart(User user, Product product, int quantity) {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(product.getProdId());
        request.setQuantity(quantity);
        cartService.addToCart(user.getUserEmail(), request);
    }

    private OrderRequest orderRequest(Long productId, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        OrderRequest request = new OrderRequest();
        request.setOrderItems(List.of(item));
        return request;
    }
}
