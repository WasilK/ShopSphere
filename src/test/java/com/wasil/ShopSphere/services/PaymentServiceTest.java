package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.payment.PaymentRequest;
import com.wasil.ShopSphere.dto.payment.PaymentResponse;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.*;
import com.wasil.ShopSphere.repositories.OrderRepository;
import com.wasil.ShopSphere.repositories.PaymentRepository;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;


    // ---------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------

    private User createUser(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setUserEmail(email);
        return user;
    }

    private Order createOrder(User user) {
        Order order = new Order();

        order.setOrderId(1L);
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("1000.00"));

        return order;
    }

    private PaymentRequest createPaymentRequest() {
        PaymentRequest request = new PaymentRequest();

        // Change this if your enum has a different value
        request.setPaymentMethod(PaymentMethod.CARD);

        return request;
    }


    // =========================================================
    // 1. SUCCESSFUL PAYMENT
    // =========================================================

    @Test
    void shouldProcessPaymentSuccessfully() {

        // Arrange
        String email = "test@gmail.com";

        User user = createUser(1L, email);
        Order order = createOrder(user);
        PaymentRequest request = createPaymentRequest();

        Payment savedPayment = new Payment();
        savedPayment.setPaymentId(10L);
        savedPayment.setOrder(order);
        savedPayment.setAmount(order.getTotalAmount());
        savedPayment.setPaymentMethod(request.getPaymentMethod());
        savedPayment.setPaymentStatus(PaymentStatus.PENDING);
        savedPayment.setTransactionId("transaction-123");

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrder_OrderId(1L))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        // Act
        PaymentResponse response =
                paymentService.processPayment(email, request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(10L, response.getPaymentId());
        assertEquals(1L, response.getOrderId());
        assertEquals(new BigDecimal("1000.00"), response.getAmount());
        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals(request.getPaymentMethod(), response.getPaymentMethod());
        assertEquals("transaction-123", response.getTransactionId());

        verify(userRepository).findByUserEmail(email);
        verify(orderRepository).findById(1L);
        verify(paymentRepository).existsByOrder_OrderId(1L);
        verify(orderRepository).save(order);
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }


    // =========================================================
    // 2. USER NOT FOUND
    // =========================================================

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        // Arrange
        String email = "unknown@gmail.com";

        PaymentRequest request = createPaymentRequest();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.empty());

        // Act + Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> paymentService.processPayment(email, request, 1L)
        );

        assertEquals(
                "User not found with this email : " + email,
                exception.getMessage()
        );

        verify(userRepository).findByUserEmail(email);

        verifyNoInteractions(orderRepository);
        verifyNoInteractions(paymentRepository);
    }


    // =========================================================
    // 3. ORDER NOT FOUND
    // =========================================================

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {

        // Arrange
        String email = "test@gmail.com";

        User user = createUser(1L, email);
        PaymentRequest request = createPaymentRequest();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act + Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> paymentService.processPayment(email, request, 1L)
        );

        assertEquals(
                "Order not found with id: 1",
                exception.getMessage()
        );

        verify(userRepository).findByUserEmail(email);
        verify(orderRepository).findById(1L);

        verifyNoInteractions(paymentRepository);
    }


    // =========================================================
    // 4. USER TRYING TO PAY FOR SOMEONE ELSE'S ORDER
    // =========================================================

    @Test
    void shouldThrowExceptionWhenOrderBelongsToAnotherUser() {

        // Arrange
        String email = "test@gmail.com";

        User loggedInUser = createUser(1L, email);
        User anotherUser = createUser(2L, "other@gmail.com");

        Order order = createOrder(anotherUser);
        PaymentRequest request = createPaymentRequest();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(loggedInUser));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act + Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> paymentService.processPayment(email, request, 1L)
        );

        assertEquals(
                "User cannot make payment for another user's order",
                exception.getMessage()
        );

        verify(userRepository).findByUserEmail(email);
        verify(orderRepository).findById(1L);

        verifyNoInteractions(paymentRepository);
    }


    // =========================================================
    // 5. ORDER IS NOT PENDING
    // =========================================================

    @Test
    void shouldThrowExceptionWhenOrderIsNotPending() {

        // Arrange
        String email = "test@gmail.com";

        User user = createUser(1L, email);
        Order order = createOrder(user);

        order.setOrderStatus(OrderStatus.CONFIRMED);

        PaymentRequest request = createPaymentRequest();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // Act + Assert
        InvalidPaymentException exception = assertThrows(
                InvalidPaymentException.class,
                () -> paymentService.processPayment(email, request, 1L)
        );

        assertEquals(
                "Payment cannot be processed for order with status: CONFIRMED",
                exception.getMessage()
        );

        verify(orderRepository).findById(1L);

        verifyNoInteractions(paymentRepository);
    }


    // =========================================================
    // 6. PAYMENT ALREADY EXISTS
    // =========================================================

    @Test
    void shouldThrowExceptionWhenPaymentAlreadyExists() {

        // Arrange
        String email = "test@gmail.com";

        User user = createUser(1L, email);
        Order order = createOrder(user);
        PaymentRequest request = createPaymentRequest();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrder_OrderId(1L))
                .thenReturn(true);

        // Act + Assert
        PaymentAlreadyProcessedException exception = assertThrows(
                PaymentAlreadyProcessedException.class,
                () -> paymentService.processPayment(email, request, 1L)
        );

        assertEquals(
                "Payment already exists for order with id: 1",
                exception.getMessage()
        );

        verify(paymentRepository).existsByOrder_OrderId(1L);

        // Payment must never be created
        verify(paymentRepository, never())
                .save(any(Payment.class));
    }


    // =========================================================
    // 7. ORDER TOTAL AMOUNT IS NULL
    // =========================================================

    @Test
    void shouldThrowExceptionWhenPaymentAmountIsNull() {

        // Arrange
        String email = "test@gmail.com";

        User user = createUser(1L, email);
        Order order = createOrder(user);

        order.setTotalAmount(null);

        PaymentRequest request = createPaymentRequest();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrder_OrderId(1L))
                .thenReturn(false);

        // Act + Assert
        InvalidPaymentException exception = assertThrows(
                InvalidPaymentException.class,
                () -> paymentService.processPayment(email, request, 1L)
        );

        assertEquals(
                "Payment amount cannot be null",
                exception.getMessage()
        );

        verify(paymentRepository).existsByOrder_OrderId(1L);

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }


    // =========================================================
    // 8. PAYMENT PROCESSING FAILS
    // =========================================================

    @Test
    void shouldMarkPaymentAsFailedWhenProcessingFails() {

        // Arrange
        String email = "test@gmail.com";

        User user = createUser(1L, email);
        Order order = createOrder(user);
        PaymentRequest request = createPaymentRequest();

        Payment payment = new Payment();

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrder_OrderId(1L))
                .thenReturn(false);

        // First save successfully creates the payment
        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(payment);

        // Make something inside the try block fail
        when(orderRepository.save(order))
                .thenThrow(new RuntimeException("Database error"));

        // Act + Assert
        PaymentFailedException exception = assertThrows(
                PaymentFailedException.class,
                () -> paymentService.processPayment(email, request, 1L)
        );

        assertEquals(
                "Payment processing failed",
                exception.getMessage()
        );

        // Payment should be marked FAILED
        assertEquals(
                PaymentStatus.FAILED,
                payment.getPaymentStatus()
        );

        // Payment saved once initially + once as FAILED
        verify(paymentRepository, times(2))
                .save(any(Payment.class));

        verify(orderRepository).save(order);
    }


    // =========================================================
    // 9. VERIFY CREATED PAYMENT DETAILS
    // =========================================================

    @Test
    void shouldCreatePaymentWithCorrectDetails() {

        // Arrange
        String email = "test@gmail.com";

        User user = createUser(1L, email);
        Order order = createOrder(user);
        PaymentRequest request = createPaymentRequest();

        Payment savedPayment = new Payment();

        savedPayment.setPaymentId(10L);
        savedPayment.setOrder(order);
        savedPayment.setAmount(order.getTotalAmount());
        savedPayment.setPaymentMethod(request.getPaymentMethod());
        savedPayment.setPaymentStatus(PaymentStatus.PENDING);
        savedPayment.setTransactionId("transaction-123");

        when(userRepository.findByUserEmail(email))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrder_OrderId(1L))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenReturn(savedPayment);

        // Act
        paymentService.processPayment(email, request, 1L);

        // Capture payment sent to repository
        ArgumentCaptor<Payment> captor =
                ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository, times(2))
                .save(captor.capture());

        Payment createdPayment =
                captor.getAllValues().get(0);

        // Assert
        assertEquals(order, createdPayment.getOrder());
        assertEquals(
                order.getTotalAmount(),
                createdPayment.getAmount()
        );
        assertEquals(
                request.getPaymentMethod(),
                createdPayment.getPaymentMethod()
        );
        assertEquals(
                PaymentStatus.PENDING,
                createdPayment.getPaymentStatus()
        );

        assertNotNull(createdPayment.getTransactionId());
    }
}
