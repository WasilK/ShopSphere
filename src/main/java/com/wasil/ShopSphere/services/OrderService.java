package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.order.OrderItemRequest;
import com.wasil.ShopSphere.dto.order.OrderRequest;
import com.wasil.ShopSphere.dto.order.OrderResponse;
import com.wasil.ShopSphere.dto.order.OrderItemResponse;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.*;
import com.wasil.ShopSphere.repositories.*;
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
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            StockMovementRepository stockMovementRepository,
            CartRepository cartRepository,
            CartService cartService) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
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
            Inventory inventory = inventoryRepository.findByProduct(product).orElseThrow(() -> new InventoryNotFoundException("Inventory not found"));
            // Check stock
            if (inventory.getCurrentStock() < itemRequest.getQuantity()) {
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
            inventory.setCurrentStock(
                    inventory.getCurrentStock() - itemRequest.getQuantity()
            );

            inventoryRepository.save(inventory);

            StockMovement stockMovement = new StockMovement();
            stockMovement.setInventory(inventory);
            stockMovement.setQuantity(itemRequest.getQuantity());
            stockMovement.setMovementType(MovementType.ORDER);

            stockMovementRepository.save(stockMovement);
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
                Inventory inventory = inventoryRepository.findByProduct(product).orElseThrow(() -> new InventoryNotFoundException("Inventory not found"));
                inventory.setCurrentStock(inventory.getCurrentStock() + item.getQuantity());
                inventoryRepository.save(inventory);
                StockMovement stockMovement = new StockMovement();
                stockMovement.setInventory(inventory);
                stockMovement.setQuantity(item.getQuantity());
                stockMovement.setMovementType(MovementType.ORDER_CANCELLED);

                stockMovementRepository.save(stockMovement);
            }
            Order savedOrder = orderRepository.save(order);
            return convertToResponse(savedOrder, orderItems);
    }

    public List<OrderResponse> getOrdersByUser(Long userId){
        List<Order> orders = orderRepository.findByUser(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId)));
        return orders.stream().map(order ->
             convertToResponse(order, orderItemRepository.findByOrder(order))).toList();
    }
    @Transactional
    public OrderResponse checkoutOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        ));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException("Cart not found"));

        List<CartItem> cartItems = cart.getCartItems();

        if (cartItems.isEmpty()) {
            throw new CartEmptyException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            Inventory inventory = inventoryRepository.findByProduct(product)
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found"
                            ));

            if (cartItem.getQuantity() > inventory.getCurrentStock()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: "
                                + product.getProdName()
                );
            }

            BigDecimal price = product.getProdPrice();

            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(price);
            orderItem.setOrder(order);

            orderItems.add(orderItem);

            totalAmount = totalAmount.add(
                    price.multiply(
                            BigDecimal.valueOf(cartItem.getQuantity())
                    )
            );

            inventory.setCurrentStock(
                    inventory.getCurrentStock()
                            - cartItem.getQuantity()
            );

            inventoryRepository.save(inventory);

            StockMovement stockMovement = new StockMovement();

            stockMovement.setInventory(inventory);
            stockMovement.setQuantity(cartItem.getQuantity());
            stockMovement.setMovementType(MovementType.ORDER);

            stockMovementRepository.save(stockMovement);
        }

        // Set total
        order.setTotalAmount(totalAmount);

        // Save Order first
        Order savedOrder = orderRepository.save(order);

        // Save OrderItems
        for (OrderItem orderItem : orderItems) {
            orderItemRepository.save(orderItem);
        }

        // Clear cart
        cartService.clearCart(userId);

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

            itemResponse.setOrderItemId(item.getOrderItemId());
            itemResponse.setProductId(item.getProduct().getProdId());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setPrice(item.getPrice());

            itemResponses.add(itemResponse);
        }

        response.setOrderItems(itemResponses);

        return response;
    }
}