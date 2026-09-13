package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.payment.PaymentRequest;
import com.wasil.ShopSphere.dto.payment.PaymentResponse;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.*;
import com.wasil.ShopSphere.repositories.OrderRepository;
import com.wasil.ShopSphere.repositories.PaymentRepository;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository, OrderRepository orderRepository){
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    public PaymentResponse processPayment(String email, PaymentRequest paymentRequest, Long orderId){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email : " + email));

        Order order = orderRepository.findById(
                orderId
        ).orElseThrow(() ->
                new OrderNotFoundException(
                        "Order not found with id: "
                                + orderId
                )
        );

        // 3. Verify that the order belongs to the logged-in user
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException(
                    "User cannot make payment for another user's order"
            );
        }

        // 4. Payment is allowed only for PENDING orders
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new InvalidPaymentException(
                    "Payment cannot be processed for order with status: "
                            + order.getOrderStatus()
            );
        }

        // 5. Check if payment already exists for this order
        if (paymentRepository.existsByOrder_OrderId(orderId)) {
            throw new PaymentAlreadyProcessedException(
                    "Payment already exists for order with id: "
                            + order.getOrderId()
            );
        }

        if (order.getTotalAmount() == null) {
            throw new InvalidPaymentException(
                    "Payment amount cannot be null"
            );
        }


        // 7. Create payment
        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());

        // 8. Initially mark payment as PENDING
        payment.setPaymentStatus(PaymentStatus.PENDING);

        // 9. Generate transaction ID
        payment.setTransactionId(
                UUID.randomUUID().toString()
        );

        // 10. Save payment
        Payment savedPayment = paymentRepository.save(payment);

        try {

            // Simulate successful payment
            savedPayment.setPaymentStatus(
                    PaymentStatus.SUCCESS
            );

            // 12. Update order after successful payment
            order.setOrderStatus(
                    OrderStatus.CONFIRMED
            );

            // 13. Save order
            orderRepository.save(order);

            // 14. Save updated payment
            Payment completedPayment =
                    paymentRepository.save(savedPayment);

            // 15. Return payment response
            return convertToResponse(completedPayment);

        } catch (Exception e) {

            // Payment failed
            savedPayment.setPaymentStatus(
                    PaymentStatus.FAILED
            );

            paymentRepository.save(savedPayment);

            throw new PaymentFailedException(
                    "Payment processing failed"
            );
        }
    }

    private PaymentResponse convertToResponse(Payment payment) {

        PaymentResponse response = new PaymentResponse();

        response.setPaymentId(payment.getPaymentId());
        response.setOrderId(payment.getOrder().getOrderId());
        response.setAmount(payment.getAmount());
        response.setPaymentStatus(payment.getPaymentStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return response;
    }
}
