package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private final IdempotencyService idempotencyService;
    private final OrderService orderService;

    public CheckoutService(
            OrderService orderService,
            IdempotencyService idempotencyService) {

        this.orderService = orderService;
        this.idempotencyService = idempotencyService;
    }


    // =========================================================
    // CHECKOUT CART
    // =========================================================

    @Transactional
    public OrderResponse createCheckoutOrder(
            String email,
            String idempotencyKey) {

        IdempotencyKey key;

        // 1. Check if this idempotency key already exists
        IdempotencyKey existingKey =
                idempotencyService.getExistingKey(
                        email,
                        idempotencyKey
                );


        // =====================================================
        // EXISTING KEY
        // =====================================================

        if (existingKey != null) {

            // -------------------------------------------------
            // COMPLETED
            // -------------------------------------------------

            if (existingKey.getStatus() == IdempotencyStatus.COMPLETED) {

                return orderService.getMyOrderById(
                        email,
                        existingKey.getOrderId()
                );
            }


            // -------------------------------------------------
            // PROCESSING
            // -------------------------------------------------

            if (existingKey.getStatus() == IdempotencyStatus.PROCESSING) {

                throw new RuntimeException(
                        "This checkout request is already being processed"
                );
            }


            // -------------------------------------------------
            // FAILED
            // -------------------------------------------------

            if (existingKey.getStatus() == IdempotencyStatus.FAILED) {

                // Reuse the SAME idempotency record
                // Do NOT create another record.
                key = idempotencyService.markProcessing(existingKey);

            } else {

                throw new RuntimeException(
                        "Invalid idempotency status"
                );
            }

        }

        // =====================================================
        // NEW KEY
        // =====================================================

        else {

            // First time this key is being used
            key = idempotencyService.createKey(
                    email,
                    idempotencyKey
            );
        }


        // =====================================================
        // PERFORM CHECKOUT
        // =====================================================

        try {

            OrderResponse orderResponse =
                    orderService.checkoutOrder(email);


            // Mark idempotency request as completed
            idempotencyService.markCompleted(
                    key,
                    orderResponse.getOrderId()
            );


            return orderResponse;


        } catch (Exception e) {

            // Checkout failed
            idempotencyService.markFailed(key);

            throw e;
        }
    }


    // =========================================================
    // DIRECT CHECKOUT
    // =========================================================

    @Transactional
    public OrderResponse directCheckoutOrder(
            String email,
            String idempotencyKey,
            OrderRequest orderRequest) {

        IdempotencyKey key;

        // 1. Check existing idempotency key
        IdempotencyKey existingKey =
                idempotencyService.getExistingKey(
                        email,
                        idempotencyKey
                );


        // =====================================================
        // EXISTING KEY
        // =====================================================

        if (existingKey != null) {

            // -------------------------------------------------
            // COMPLETED
            // -------------------------------------------------

            if (existingKey.getStatus() == IdempotencyStatus.COMPLETED) {

                return orderService.getMyOrderById(
                        email,
                        existingKey.getOrderId()
                );
            }


            // -------------------------------------------------
            // PROCESSING
            // -------------------------------------------------

            if (existingKey.getStatus() == IdempotencyStatus.PROCESSING) {

                throw new RuntimeException(
                        "This checkout request is already being processed"
                );
            }


            // -------------------------------------------------
            // FAILED
            // -------------------------------------------------

            if (existingKey.getStatus() == IdempotencyStatus.FAILED) {

                // Reuse the existing record
                key = idempotencyService.markProcessing(existingKey);

            } else {

                throw new RuntimeException(
                        "Invalid idempotency status"
                );
            }

        }

        // =====================================================
        // NEW KEY
        // =====================================================

        else {

            key = idempotencyService.createKey(
                    email,
                    idempotencyKey
            );
        }


        // =====================================================
        // PERFORM DIRECT CHECKOUT
        // =====================================================

        try {

            OrderResponse orderResponse =
                    orderService.createOrder(
                            email,
                            orderRequest
                    );


            // Mark request as successfully completed
            idempotencyService.markCompleted(
                    key,
                    orderResponse.getOrderId()
            );


            return orderResponse;


        } catch (Exception e) {

            // Mark request as failed
            idempotencyService.markFailed(key);

            throw e;
        }
    }
}