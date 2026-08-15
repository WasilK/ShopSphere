package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.order.OrderItemRequest;
import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.dto.order.OrderItemResponse;
import com.wasil.ShopSphere.exceptions.ProductNotFoundException;
import com.wasil.ShopSphere.exceptions.UserNotFoundException;
import com.wasil.ShopSphere.model.Order;
import com.wasil.ShopSphere.model.OrderItem;
import com.wasil.ShopSphere.model.OrderStatus;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.model.User;
import com.wasil.ShopSphere.repositories.OrderItemRepository;
import com.wasil.ShopSphere.repositories.OrderRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import com.wasil.ShopSphere.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Find User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + request.getUserId()
                        ));

        // 2. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        // 3. Process every item
        for (OrderItemRequest itemRequest : request.getOrderItems()) {

            // Find Product
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    "Product not found with id: "
                                            + itemRequest.getProductId()
                            ));

            // Check quantity
            if (itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be greater than 0"
                );
            }

            // Check stock
            if (product.getProdStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: "
                                + product.getProdName()
                );
            }

            // Get CURRENT product price
            BigDecimal price = product.getProdPrice();

            // Create OrderItem
            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(price);

            orderItems.add(orderItem);

            // Calculate item total
            BigDecimal itemTotal =
                    price.multiply(
                            BigDecimal.valueOf(itemRequest.getQuantity())
                    );

            totalAmount = totalAmount.add(itemTotal);

            // Reduce stock
            product.setProdStock(
                    product.getProdStock() - itemRequest.getQuantity()
            );

            productRepository.save(product);
        }

        // 4. Set total
        order.setTotalAmount(totalAmount);

        // 5. Save Order
        Order savedOrder = orderRepository.save(order);

        // 6. Save OrderItems
        for (OrderItem orderItem : orderItems) {
            orderItemRepository.save(orderItem);
        }

        // 7. Convert to response
        return convertToResponse(savedOrder, orderItems);
    }


    private OrderResponse convertToResponse(
            Order order,
            List<OrderItem> orderItems) {

        OrderResponse response = new OrderResponse();

        response.setOrderId(order.getOrderId());
        response.setUserId(order.getUser().getUserId());
        response.setOrderStatus(order.getOrderStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderCreatedAt(order.getOrderCreatedAt());
        response.setOrderUpdatedAt(order.getOrderUpdatedAt());

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItem item : orderItems) {

            OrderItemResponse itemResponse = new OrderItemResponse();

            itemResponse.setOrderItemId(item.getOrder().getOrderId());
            itemResponse.setProductId(item.getProduct().getProdId());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());

            itemResponses.add(itemResponse);
        }

        response.setOrderItems(itemResponses);

        return response;
    }
}