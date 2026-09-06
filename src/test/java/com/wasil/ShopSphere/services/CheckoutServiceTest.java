package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.IdempotencyKey;
import com.wasil.ShopSphere.model.IdempotencyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CheckoutService checkoutService;

    private IdempotencyKey idempotencyKey;
    private OrderResponse orderResponse;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {

        idempotencyKey = new IdempotencyKey();

        orderResponse = new OrderResponse();
        orderResponse.setOrderId(100L);

        orderRequest = new OrderRequest();
    }


    // =========================================================
    // createCheckoutOrder()
    // =========================================================

    @Test
    void shouldCreateCheckoutOrderSuccessfullyWithNewKey() {

        // Arrange
        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(null);

        when(idempotencyService.createKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        when(orderService.checkoutOrder("test@gmail.com"))
                .thenReturn(orderResponse);

        // Act
        OrderResponse response =
                checkoutService.createCheckoutOrder(
                        "test@gmail.com",
                        "key-123"
                );

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());

        verify(idempotencyService)
                .getExistingKey("test@gmail.com", "key-123");

        verify(idempotencyService)
                .createKey("test@gmail.com", "key-123");

        verify(orderService)
                .checkoutOrder("test@gmail.com");

        verify(idempotencyService)
                .markCompleted(idempotencyKey, 100L);
    }


    @Test
    void shouldReturnExistingOrderWhenKeyIsCompleted() {

        // Arrange
        idempotencyKey.setStatus(IdempotencyStatus.COMPLETED);
        idempotencyKey.setOrderId(100L);

        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        when(orderService.getMyOrderById(
                "test@gmail.com",
                100L
        )).thenReturn(orderResponse);

        // Act
        OrderResponse response =
                checkoutService.createCheckoutOrder(
                        "test@gmail.com",
                        "key-123"
                );

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());

        verify(orderService)
                .getMyOrderById("test@gmail.com", 100L);

        // Checkout must NOT run again
        verify(orderService, never())
                .checkoutOrder(anyString());

        verify(idempotencyService, never())
                .markProcessing(any());

        verify(idempotencyService, never())
                .markCompleted(any(), anyLong());
    }


    @Test
    void shouldThrowExceptionWhenCheckoutIsAlreadyProcessing() {

        // Arrange
        idempotencyKey.setStatus(IdempotencyStatus.PROCESSING);

        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        // Act & Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> checkoutService.createCheckoutOrder(
                                "test@gmail.com",
                                "key-123"
                        )
                );

        assertEquals(
                "This checkout request is already being processed",
                exception.getMessage()
        );

        verify(orderService, never())
                .checkoutOrder(anyString());

        verify(idempotencyService, never())
                .markProcessing(any());
    }


    @Test
    void shouldReuseFailedKeyAndProcessCheckoutAgain() {

        // Arrange
        idempotencyKey.setStatus(IdempotencyStatus.FAILED);

        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        when(idempotencyService.markProcessing(idempotencyKey))
                .thenReturn(idempotencyKey);

        when(orderService.checkoutOrder("test@gmail.com"))
                .thenReturn(orderResponse);

        // Act
        OrderResponse response =
                checkoutService.createCheckoutOrder(
                        "test@gmail.com",
                        "key-123"
                );

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());

        verify(idempotencyService)
                .markProcessing(idempotencyKey);

        verify(orderService)
                .checkoutOrder("test@gmail.com");

        verify(idempotencyService)
                .markCompleted(idempotencyKey, 100L);

        // Should NOT create a new idempotency record
        verify(idempotencyService, never())
                .createKey(anyString(), anyString());
    }


    @Test
    void shouldThrowExceptionForInvalidIdempotencyStatus() {

        // Arrange
        // null status doesn't match COMPLETED, PROCESSING or FAILED
        idempotencyKey.setStatus(null);

        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        // Act & Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> checkoutService.createCheckoutOrder(
                                "test@gmail.com",
                                "key-123"
                        )
                );

        assertEquals(
                "Invalid idempotency status",
                exception.getMessage()
        );

        verify(orderService, never())
                .checkoutOrder(anyString());
    }


    @Test
    void shouldMarkKeyAsFailedWhenCheckoutFails() {

        // Arrange
        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(null);

        when(idempotencyService.createKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        when(orderService.checkoutOrder("test@gmail.com"))
                .thenThrow(new RuntimeException("Checkout failed"));

        // Act & Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> checkoutService.createCheckoutOrder(
                                "test@gmail.com",
                                "key-123"
                        )
                );

        assertEquals("Checkout failed", exception.getMessage());

        verify(idempotencyService)
                .markFailed(idempotencyKey);

        verify(idempotencyService, never())
                .markCompleted(any(), anyLong());
    }


    // =========================================================
    // directCheckoutOrder()
    // =========================================================

    @Test
    void shouldCreateDirectCheckoutOrderSuccessfullyWithNewKey() {

        // Arrange
        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(null);

        when(idempotencyService.createKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        when(orderService.createOrder(
                "test@gmail.com",
                orderRequest
        )).thenReturn(orderResponse);

        // Act
        OrderResponse response =
                checkoutService.directCheckoutOrder(
                        "test@gmail.com",
                        "key-123",
                        orderRequest
                );

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());

        verify(idempotencyService)
                .getExistingKey("test@gmail.com", "key-123");

        verify(idempotencyService)
                .createKey("test@gmail.com", "key-123");

        verify(orderService)
                .createOrder("test@gmail.com", orderRequest);

        verify(idempotencyService)
                .markCompleted(idempotencyKey, 100L);
    }


    @Test
    void shouldReturnExistingOrderForCompletedDirectCheckout() {

        // Arrange
        idempotencyKey.setStatus(IdempotencyStatus.COMPLETED);
        idempotencyKey.setOrderId(100L);

        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        when(orderService.getMyOrderById(
                "test@gmail.com",
                100L
        )).thenReturn(orderResponse);

        // Act
        OrderResponse response =
                checkoutService.directCheckoutOrder(
                        "test@gmail.com",
                        "key-123",
                        orderRequest
                );

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());

        verify(orderService)
                .getMyOrderById("test@gmail.com", 100L);

        verify(orderService, never())
                .createOrder(anyString(), any(OrderRequest.class));

        verify(idempotencyService, never())
                .markProcessing(any());
    }


    @Test
    void shouldThrowExceptionWhenDirectCheckoutIsAlreadyProcessing() {

        // Arrange
        idempotencyKey.setStatus(IdempotencyStatus.PROCESSING);

        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        // Act & Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> checkoutService.directCheckoutOrder(
                                "test@gmail.com",
                                "key-123",
                                orderRequest
                        )
                );

        assertEquals(
                "This checkout request is already being processed",
                exception.getMessage()
        );

        verify(orderService, never())
                .createOrder(anyString(), any(OrderRequest.class));

        verify(idempotencyService, never())
                .markProcessing(any());
    }


    @Test
    void shouldReuseFailedKeyForDirectCheckout() {

        // Arrange
        idempotencyKey.setStatus(IdempotencyStatus.FAILED);

        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        when(idempotencyService.markProcessing(idempotencyKey))
                .thenReturn(idempotencyKey);

        when(orderService.createOrder(
                "test@gmail.com",
                orderRequest
        )).thenReturn(orderResponse);

        // Act
        OrderResponse response =
                checkoutService.directCheckoutOrder(
                        "test@gmail.com",
                        "key-123",
                        orderRequest
                );

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.getOrderId());

        verify(idempotencyService)
                .markProcessing(idempotencyKey);

        verify(orderService)
                .createOrder("test@gmail.com", orderRequest);

        verify(idempotencyService)
                .markCompleted(idempotencyKey, 100L);

        verify(idempotencyService, never())
                .createKey(anyString(), anyString());
    }


    @Test
    void shouldThrowExceptionForInvalidDirectCheckoutStatus() {

        // Arrange
        idempotencyKey.setStatus(null);

        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        // Act & Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> checkoutService.directCheckoutOrder(
                                "test@gmail.com",
                                "key-123",
                                orderRequest
                        )
                );

        assertEquals(
                "Invalid idempotency status",
                exception.getMessage()
        );

        verify(orderService, never())
                .createOrder(anyString(), any(OrderRequest.class));
    }


    @Test
    void shouldMarkKeyAsFailedWhenDirectCheckoutFails() {

        // Arrange
        when(idempotencyService.getExistingKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(null);

        when(idempotencyService.createKey(
                "test@gmail.com",
                "key-123"
        )).thenReturn(idempotencyKey);

        when(orderService.createOrder(
                "test@gmail.com",
                orderRequest
        )).thenThrow(new RuntimeException("Order creation failed"));

        // Act & Assert
        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> checkoutService.directCheckoutOrder(
                                "test@gmail.com",
                                "key-123",
                                orderRequest
                        )
                );

        assertEquals("Order creation failed", exception.getMessage());

        verify(idempotencyService)
                .markFailed(idempotencyKey);

        verify(idempotencyService, never())
                .markCompleted(any(), anyLong());
    }
}