package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.order.*;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.*;
import com.wasil.ShopSphere.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private User otherUser;
    private Product product;
    private Product inactiveProduct;
    private Inventory inventory;
    private Order order;
    private OrderItem orderItem;
    private Cart cart;
    private CartItem cartItem;
    private Payment payment;
    private OrderRequest orderRequest;
    private OrderItemRequest orderItemRequest;


    @BeforeEach
    void setUp() {

        // =========================
        // USER
        // =========================

        user = new User();
        user.setUserId(1L);
        user.setUserEmail("user@gmail.com");
        user.setAddresses(new ArrayList<>());

        // Add dummy address because createOrder/checkoutOrder
        // requires the user to have an address.
        user.getAddresses().add(new Address());


        otherUser = new User();
        otherUser.setUserId(2L);
        otherUser.setUserEmail("other@gmail.com");
        otherUser.setAddresses(new ArrayList<>());


        // =========================
        // PRODUCT
        // =========================

        product = new Product();
        product.setProdId(1L);
        product.setProdName("Laptop");
        product.setProdPrice(new BigDecimal("1000.00"));
        product.setProdIsActive(true);

        inactiveProduct = new Product();
        inactiveProduct.setProdId(2L);
        inactiveProduct.setProdName("Inactive Laptop");
        inactiveProduct.setProdPrice(new BigDecimal("500.00"));
        inactiveProduct.setProdIsActive(false);


        // =========================
        // INVENTORY
        // =========================

        inventory = new Inventory();
        inventory.setInventoryId(10L);
        inventory.setProduct(product);
        inventory.setCurrentStock(10);


        // =========================
        // ORDER
        // =========================

        order = new Order();
        order.setOrderId(100L);
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1000.00"));


        // =========================
        // ORDER ITEM
        // =========================

        orderItem = new OrderItem();
        orderItem.setOrderItemId(1000L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(1);
        orderItem.setPrice(new BigDecimal("1000.00"));


        // =========================
        // CART
        // =========================

        cart = new Cart();
        cart.setCartId(50L);
        cart.setUser(user);

        cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart.setCartItems(new ArrayList<>());
        cart.getCartItems().add(cartItem);


        // =========================
        // PAYMENT
        // =========================

        payment = new Payment();
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setOrder(order);


        // =========================
        // ORDER REQUEST
        // =========================

        orderItemRequest = new OrderItemRequest();
        orderItemRequest.setProductId(1L);
        orderItemRequest.setQuantity(1);

        orderRequest = new OrderRequest();
        orderRequest.setOrderItems(List.of(orderItemRequest));
    }


    // =========================================================
    // CREATE ORDER
    // =========================================================

    @Test
    void shouldCreateOrderSuccessfully() {

        // Arrange
        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(orderItemRepository.save(any(OrderItem.class)))
                .thenReturn(orderItem);

        // Act
        OrderResponse response =
                orderService.createOrder(
                        "user@gmail.com",
                        orderRequest
                );

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());
        assertEquals(1L, response.getUserId());
        assertEquals(
                new BigDecimal("1000.00"),
                response.getTotalAmount()
        );
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());

        verify(userRepository)
                .findByUserEmail("user@gmail.com");

        verify(productRepository)
                .findById(1L);

        verify(inventoryRepository)
                .findByProduct(product);

        verify(orderRepository)
                .save(any(Order.class));

        verify(orderItemRepository)
                .save(any(OrderItem.class));
    }


    @Test
    void shouldThrowExceptionWhenCreatingOrderForUnknownUser() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> orderService.createOrder(
                        "user@gmail.com",
                        orderRequest
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenUserHasNoAddress() {

        user.setAddresses(new ArrayList<>());

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                "user@gmail.com",
                                orderRequest
                        )
                );

        assertEquals(
                "User must have an address before placing the order.",
                exception.getMessage()
        );

        verify(productRepository, never())
                .findById(anyLong());
    }


    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> orderService.createOrder(
                        "user@gmail.com",
                        orderRequest
                )
        );

        verify(inventoryRepository, never())
                .findByProduct(any(Product.class));
    }


    @Test
    void shouldThrowExceptionWhenProductIsInactive() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(inactiveProduct));

        ProductInActiveException exception =
                assertThrows(
                        ProductInActiveException.class,
                        () -> orderService.createOrder(
                                "user@gmail.com",
                                orderRequest
                        )
                );

        assertEquals(
                "Product is not active.",
                exception.getMessage()
        );

        verify(inventoryRepository, never())
                .findByProduct(any(Product.class));
    }


    @Test
    void shouldThrowExceptionWhenInventoryDoesNotExist() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> orderService.createOrder(
                        "user@gmail.com",
                        orderRequest
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenQuantityIsZeroOrNegative() {

        orderItemRequest.setQuantity(0);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.createOrder(
                                "user@gmail.com",
                                orderRequest
                        )
                );

        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        orderItemRequest.setQuantity(20);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        InsufficientStockException exception =
                assertThrows(
                        InsufficientStockException.class,
                        () -> orderService.createOrder(
                                "user@gmail.com",
                                orderRequest
                        )
                );

        assertEquals(
                "Insufficient stock for product: Laptop",
                exception.getMessage()
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldCalculateOrderTotalCorrectly() {

        orderItemRequest.setQuantity(3);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order savedOrder = invocation.getArgument(0);
                    savedOrder.setOrderId(100L);
                    return savedOrder;
                });

        when(orderItemRepository.save(any(OrderItem.class)))
                .thenReturn(orderItem);

        OrderResponse response =
                orderService.createOrder(
                        "user@gmail.com",
                        orderRequest
                );

        // 1000 × 3
        assertEquals(
                new BigDecimal("3000.00"),
                response.getTotalAmount()
        );
    }


    // =========================================================
    // CHECKOUT CART
    // =========================================================

    @Test
    void shouldCheckoutCartSuccessfully() {

        // Arrange
        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(orderItemRepository.save(any(OrderItem.class)))
                .thenReturn(orderItem);

        // Act
        OrderResponse response =
                orderService.checkoutOrder("user@gmail.com");

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());

        verify(cartRepository)
                .findByUser(user);

        verify(orderRepository)
                .save(any(Order.class));

        verify(orderItemRepository)
                .save(any(OrderItem.class));

        verify(cartService)
                .clearCart("user@gmail.com");
    }


    @Test
    void shouldThrowExceptionWhenCheckoutUserDoesNotExist() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> orderService.checkoutOrder(
                        "user@gmail.com"
                )
        );

        verify(cartRepository, never())
                .findByUser(any(User.class));
    }


    @Test
    void shouldThrowExceptionWhenCartDoesNotExist() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.empty());

        assertThrows(
                CartNotFoundException.class,
                () -> orderService.checkoutOrder(
                        "user@gmail.com"
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {

        cart.setCartItems(new ArrayList<>());

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        assertThrows(
                CartEmptyException.class,
                () -> orderService.checkoutOrder(
                        "user@gmail.com"
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenCheckoutUserHasNoAddress() {

        user.setAddresses(new ArrayList<>());

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.checkoutOrder(
                                "user@gmail.com"
                        )
                );

        assertEquals(
                "User must have an address before placing the order.",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenCartProductIsInactive() {

        cartItem.setProduct(inactiveProduct);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        assertThrows(
                ProductInActiveException.class,
                () -> orderService.checkoutOrder(
                        "user@gmail.com"
                )
        );

        verify(inventoryRepository, never())
                .findByProduct(any(Product.class));
    }


    @Test
    void shouldThrowExceptionWhenCheckoutStockIsInsufficient() {

        cartItem.setQuantity(20);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(cartRepository.findByUser(user))
                .thenReturn(Optional.of(cart));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.checkoutOrder(
                        "user@gmail.com"
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    // =========================================================
    // GET ORDER BY ID
    // =========================================================

    @Test
    void shouldGetOrderByIdSuccessfully() {

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        OrderResponse response =
                orderService.getOrderById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getOrderId());
        assertEquals(1L, response.getUserId());
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertEquals(
                new BigDecimal("1000.00"),
                response.getTotalAmount()
        );

        assertEquals(1, response.getOrderItems().size());

        verify(orderRepository)
                .findById(100L);

        verify(orderItemRepository)
                .findByOrder(order);
    }


    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        when(orderRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(100L)
        );

        verify(orderItemRepository, never())
                .findByOrder(any(Order.class));
    }


    // =========================================================
    // GET MY ORDER BY ID
    // =========================================================

    @Test
    void shouldGetMyOrderByIdSuccessfully() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        OrderResponse response =
                orderService.getMyOrderById(
                        "user@gmail.com",
                        100L
                );

        assertNotNull(response);
        assertEquals(100L, response.getOrderId());

        verify(orderItemRepository)
                .findByOrder(order);
    }


    @Test
    void shouldThrowExceptionWhenGettingMyOrderForUnknownUser() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> orderService.getMyOrderById(
                        "user@gmail.com",
                        100L
                )
        );

        verify(orderRepository, never())
                .findById(anyLong());
    }


    @Test
    void shouldThrowExceptionWhenMyOrderDoesNotExist() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getMyOrderById(
                        "user@gmail.com",
                        100L
                )
        );
    }


    @Test
    void shouldThrowExceptionWhenUserAccessesAnotherUsersOrder() {

        order.setUser(otherUser);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        UnauthorizedException exception =
                assertThrows(
                        UnauthorizedException.class,
                        () -> orderService.getMyOrderById(
                                "user@gmail.com",
                                100L
                        )
                );

        assertEquals(
                "User cannot access other users orders",
                exception.getMessage()
        );

        verify(orderItemRepository, never())
                .findByOrder(any(Order.class));
    }


    // =========================================================
    // GET ALL ORDERS
    // =========================================================

    @Test
    void shouldGetAllOrdersSuccessfully() {

        when(orderRepository.findAll())
                .thenReturn(List.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        List<OrderResponse> responses =
                orderService.getAllOrders();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getOrderId());

        verify(orderRepository)
                .findAll();

        verify(orderItemRepository)
                .findByOrder(order);
    }


    @Test
    void shouldReturnEmptyListWhenNoOrdersExist() {

        when(orderRepository.findAll())
                .thenReturn(List.of());

        List<OrderResponse> responses =
                orderService.getAllOrders();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(orderRepository)
                .findAll();
    }


    // =========================================================
    // GET MY ALL ORDERS
    // =========================================================

    @Test
    void shouldGetMyAllOrdersSuccessfully() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByUser(user))
                .thenReturn(List.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        List<OrderResponse> responses =
                orderService.getMyAllOrders(
                        "user@gmail.com"
                );

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getOrderId());

        verify(orderRepository)
                .findByUser(user);
    }


    @Test
    void shouldThrowExceptionWhenGettingMyAllOrdersForUnknownUser() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> orderService.getMyAllOrders(
                        "user@gmail.com"
                )
        );

        verify(orderRepository, never())
                .findByUser(any(User.class));
    }


    @Test
    void shouldGetOrdersByUserSuccessfully() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByUser(user))
                .thenReturn(List.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        List<OrderResponse> responses =
                orderService.getOrdersByUser(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getOrderId());

        verify(userRepository)
                .findById(1L);

        verify(orderRepository)
                .findByUser(user);
    }


    @Test
    void shouldThrowExceptionWhenGettingOrdersForUnknownUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> orderService.getOrdersByUser(1L)
        );

        verify(orderRepository, never())
                .findByUser(any(User.class));
    }


    // =========================================================
    // CANCEL ORDER
    // =========================================================

    @Test
    void shouldCancelPendingOrderSuccessfully() {

        order.setOrderStatus(OrderStatus.PENDING);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response =
                orderService.cancelOrder(
                        "user@gmail.com",
                        100L
                );

        assertNotNull(response);
        assertEquals(
                OrderStatus.CANCELLED,
                response.getOrderStatus()
        );

        assertEquals(11, inventory.getCurrentStock());

        verify(inventoryRepository)
                .save(inventory);

        verify(stockMovementRepository)
                .save(any(StockMovement.class));

        verify(orderRepository)
                .save(order);
    }


    @Test
    void shouldCancelConfirmedOrderSuccessfully() {

        order.setOrderStatus(OrderStatus.CONFIRMED);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderResponse response =
                orderService.cancelOrder(
                        "user@gmail.com",
                        100L
                );

        assertEquals(
                OrderStatus.CANCELLED,
                response.getOrderStatus()
        );

        verify(stockMovementRepository)
                .save(any(StockMovement.class));
    }


    @Test
    void shouldThrowExceptionWhenCancellingAnotherUsersOrder() {

        order.setUser(otherUser);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        assertThrows(
                UnauthorizedException.class,
                () -> orderService.cancelOrder(
                        "user@gmail.com",
                        100L
                )
        );

        verify(orderItemRepository, never())
                .findByOrder(any(Order.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenCancellingNonExistingOrder() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(
                        "user@gmail.com",
                        100L
                )
        );
    }


    @Test
    void shouldThrowExceptionWhenCancellingNonCancellableOrder() {

        order.setOrderStatus(OrderStatus.SHIPPED);

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        OrderCannotBeCancelled exception =
                assertThrows(
                        OrderCannotBeCancelled.class,
                        () -> orderService.cancelOrder(
                                "user@gmail.com",
                                100L
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("cannot be cancelled")
        );

        verify(orderItemRepository, never())
                .findByOrder(any(Order.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenInventoryMissingDuringCancellation() {

        when(userRepository.findByUserEmail("user@gmail.com"))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> orderService.cancelOrder(
                        "user@gmail.com",
                        100L
                )
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    // =========================================================
    // UPDATE ORDER STATUS
    // =========================================================

    @Test
    void shouldUpdatePendingToConfirmed() {

        order.setOrderStatus(OrderStatus.PENDING);

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest();

        request.setNewStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        OrderResponse response =
                orderService.updateOrderStatus(
                        100L,
                        request
                );

        assertEquals(
                OrderStatus.CONFIRMED,
                response.getOrderStatus()
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldUpdateConfirmedToProcessing() {

        order.setOrderStatus(OrderStatus.CONFIRMED);

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest();

        request.setNewStatus(OrderStatus.PROCESSING);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        OrderResponse response =
                orderService.updateOrderStatus(
                        100L,
                        request
                );

        assertEquals(
                OrderStatus.PROCESSING,
                response.getOrderStatus()
        );
    }


    @Test
    void shouldUpdateProcessingToShipped() {

        order.setOrderStatus(OrderStatus.PROCESSING);

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest();

        request.setNewStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        OrderResponse response =
                orderService.updateOrderStatus(
                        100L,
                        request
                );

        assertEquals(
                OrderStatus.SHIPPED,
                response.getOrderStatus()
        );
    }


    @Test
    void shouldUpdateShippedToDelivered() {

        order.setOrderStatus(OrderStatus.SHIPPED);

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest();

        request.setNewStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        OrderResponse response =
                orderService.updateOrderStatus(
                        100L,
                        request
                );

        assertEquals(
                OrderStatus.DELIVERED,
                response.getOrderStatus()
        );
    }


    @Test
    void shouldRejectInvalidOrderStatusTransition() {

        order.setOrderStatus(OrderStatus.PENDING);

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest();

        request.setNewStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        OrderStatusCannotBeUpdatedException exception =
                assertThrows(
                        OrderStatusCannotBeUpdatedException.class,
                        () -> orderService.updateOrderStatus(
                                100L,
                                request
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("Order status cannot be updated")
        );
    }


    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingOrderStatus() {

        OrderStatusUpdateRequest request =
                new OrderStatusUpdateRequest();

        request.setNewStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrderStatus(
                        100L,
                        request
                )
        );
    }


    // =========================================================
    // HANDLE PAYMENT SUCCESS
    // =========================================================

    @Test
    void shouldHandlePaymentSuccessAndReduceInventory() {

        order.setOrderStatus(OrderStatus.PENDING);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrder_OrderId(100L))
                .thenReturn(Optional.of(payment));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        orderService.handlePaymentSuccess(100L);

        // 10 - 1
        assertEquals(9, inventory.getCurrentStock());

        assertEquals(
                OrderStatus.CONFIRMED,
                order.getOrderStatus()
        );

        verify(inventoryRepository)
                .save(inventory);

        verify(stockMovementRepository)
                .save(any(StockMovement.class));

        verify(orderRepository)
                .save(order);
    }


    @Test
    void shouldThrowExceptionWhenPaymentDoesNotExist() {

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrder_OrderId(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                PaymentNotFoundException.class,
                () -> orderService.handlePaymentSuccess(100L)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }


    @Test
    void shouldThrowExceptionWhenPaymentIsNotSuccessful() {

        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrder_OrderId(100L))
                .thenReturn(Optional.of(payment));

        InvalidPaymentException exception =
                assertThrows(
                        InvalidPaymentException.class,
                        () -> orderService.handlePaymentSuccess(100L)
                );

        assertEquals(
                "Payment is not successful. Inventory cannot be reduced.",
                exception.getMessage()
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }


    @Test
    void shouldReturnWithoutProcessingAlreadyConfirmedOrder() {

        order.setOrderStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrder_OrderId(100L))
                .thenReturn(Optional.of(payment));

        orderService.handlePaymentSuccess(100L);

        verify(orderItemRepository, never())
                .findByOrder(any(Order.class));

        verify(inventoryRepository, never())
                .findByProduct(any(Product.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenOrderDoesNotExistDuringPaymentSuccess() {

        when(orderRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.handlePaymentSuccess(100L)
        );

        verify(paymentRepository, never())
                .findByOrder_OrderId(anyLong());
    }


    @Test
    void shouldThrowExceptionWhenInventoryDoesNotExistDuringPaymentSuccess() {

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrder_OrderId(100L))
                .thenReturn(Optional.of(payment));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> orderService.handlePaymentSuccess(100L)
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }


    @Test
    void shouldThrowExceptionWhenPaymentSuccessHasInsufficientStock() {

        inventory.setCurrentStock(0);

        when(orderRepository.findById(100L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.findByOrder_OrderId(100L))
                .thenReturn(Optional.of(payment));

        when(orderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> orderService.handlePaymentSuccess(100L)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(orderRepository, never())
                .save(any(Order.class));
    }
}