package com.wasil.ShopSphere.repositories;

import com.wasil.ShopSphere.model.Order;
import com.wasil.ShopSphere.model.Payment;
import com.wasil.ShopSphere.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder(Order order);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByOrder_OrderId(Long orderId);

    boolean existsByOrder_OrderId(Long orderId);

    boolean existsByTransactionId(String transactionId);

    long countByPaymentStatus(PaymentStatus paymentStatus);
}
