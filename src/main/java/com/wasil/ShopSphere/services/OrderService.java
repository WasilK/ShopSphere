package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.order.OrderItemRequest;
import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.dto.order.OrderItemResponse;
import com.wasil.ShopSphere.exceptions.*;
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

import static java.util.stream.Collectors.toList;

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

            // Check stock
            if (product.getProdStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
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

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        return convertToResponse(order, orderItems);
    }

    public List<OrderResponse> getAllOrders(){
        return orderRepository.findAll().stream().map(order -> convertToResponse(order, orderItemRepository.findByOrder(order))).toList();
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        if (order.getOrderStatus() != OrderStatus.PENDING &&
                order.getOrderStatus() != OrderStatus.CONFIRMED) {

            throw new OrderCannotBeCancelled(
                    "Order with id: " + orderId +
                            " cannot be cancelled as it is in " +
                            order.getOrderStatus() + " status."
            );
        }
            order.setOrderStatus(OrderStatus.CANCELLED);
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            for(OrderItem item : orderItems){
                Product product = item.getProduct();
                product.setProdStock(product.getProdStock() + item.getQuantity());
                productRepository.save(product);
            }
            Order savedOrder = orderRepository.save(order);
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