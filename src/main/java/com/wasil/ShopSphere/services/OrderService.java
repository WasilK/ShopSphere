package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.order.*;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.*;
import com.wasil.ShopSphere.repositories.*;
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

    // =========================================================
    // CREATE ORDER DIRECTLY
    // =========================================================

    @Transactional
    public OrderResponse createOrder(String email, OrderRequest request) {
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email : " + email));

        if(user.getAddresses().isEmpty()){
            throw new IllegalArgumentException("User must have an address before placing the order.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getOrderItems()) {

            Product product = productRepository.findById(
                    itemRequest.getProductId()
            ).orElseThrow(() ->
                    new ProductNotFoundException(
                            "Product not found with id: "
                                    + itemRequest.getProductId()
                    ));
            if(!product.getProdIsActive()) throw new ProductInActiveException("Product is not active.");

            Inventory inventory = inventoryRepository.findByProduct(product)
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found for product: "
                                            + product.getProdName()
                            ));

            // Check stock
            validateStock(
                    inventory,
                    itemRequest.getQuantity(),
                    product
            );

            // Create OrderItem
            OrderItem orderItem = createOrderItem(
                    order,
                    product,
                    itemRequest.getQuantity()
            );

            orderItems.add(orderItem);

            // Calculate total
            totalAmount = totalAmount.add(
                    calculateItemTotal(
                            product.getProdPrice(),
                            itemRequest.getQuantity()
                    )
            );

            // Reduce inventory + create movement
            reduceInventory(
                    inventory,
                    itemRequest.getQuantity()
            );
        }

        order.setTotalAmount(totalAmount);

        // Save Order first
        Order savedOrder = orderRepository.save(order);

        // Save OrderItems
        for (OrderItem orderItem : orderItems) {
            orderItemRepository.save(orderItem);
        }

        return convertToResponse(savedOrder, orderItems);
    }


    // =========================================================
    // CHECKOUT CART
    // =========================================================

    @Transactional
    public OrderResponse checkoutOrder(String email) {

        // 1. Find User
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email : " + email));

        // 2. Find Cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "Cart not found for user with email : " + email
                        ));

        // 3. Get Cart Items
        List<CartItem> cartItems = cart.getCartItems();

        // 4. Check empty cart
        if (cartItems.isEmpty()) {
            throw new CartEmptyException("Cart is empty");
        }

        if(user.getAddresses().isEmpty()){
            throw new IllegalArgumentException("User must have an address before placing the order.");
        }

        // 5. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        // 6. Process every CartItem
        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();
            if(!product.getProdIsActive()) throw new ProductInActiveException("Product is not active.");

            Inventory inventory = inventoryRepository.findByProduct(product)
                    .orElseThrow(() ->
                            new InventoryNotFoundException(
                                    "Inventory not found for product: "
                                            + product.getProdName()
                            ));

            int quantity = cartItem.getQuantity();

            // Validate stock
            validateStock(
                    inventory,
                    quantity,
                    product
            );

            // Create OrderItem
            OrderItem orderItem = createOrderItem(
                    order,
                    product,
                    quantity
            );

            orderItems.add(orderItem);

            // Calculate total
            totalAmount = totalAmount.add(
                    calculateItemTotal(
                            product.getProdPrice(),
                            quantity
                    )
            );

            // Reduce inventory
            reduceInventory(
                    inventory,
                    quantity
            );
        }

        // 7. Set total
        order.setTotalAmount(totalAmount);

        // 8. Save Order
        Order savedOrder = orderRepository.save(order);

        // 9. Save OrderItems
        for (OrderItem orderItem : orderItems) {
            orderItemRepository.save(orderItem);
        }

        // 10. Clear cart
        cartService.clearCart(email);

        // 11. Return response
        return convertToResponse(
                savedOrder,
                orderItems
        );
    }


    // =========================================================
    // GET ORDER BY ID
    // =========================================================

    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        ));

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(order);

        return convertToResponse(
                order,
                orderItems
        );
    }
    @Transactional
    public OrderResponse getMyOrderById(String email, Long orderId) {
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email : " + email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        ));
        if(!order.getUser().getUserId().equals(user.getUserId())) throw new UnauthorizedException("User cannot access other users orders");

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(order);

        return convertToResponse(
                order,
                orderItems
        );
    }


    // =========================================================
    // GET ALL ORDERS
    // =========================================================

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(order ->
                        convertToResponse(
                                order,
                                orderItemRepository.findByOrder(order)
                        )
                )
                .toList();
    }
    @Transactional
    public List<OrderResponse> getMyAllOrders(String email) {
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email : " + email));
        List<Order> orders = orderRepository.findByUser(user);
        return orders
                .stream()
                .map(order ->
                        convertToResponse(
                                order,
                                orderItemRepository.findByOrder(order)
                        )
                )
                .toList();
    }


    // =========================================================
    // GET ORDERS BY USER
    // =========================================================

    public List<OrderResponse> getOrdersByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + userId
                        ));

        List<Order> orders =
                orderRepository.findByUser(user);

        return orders.stream()
                .map(order ->
                        convertToResponse(
                                order,
                                orderItemRepository.findByOrder(order)
                        )
                )
                .toList();
    }


    // =========================================================
    // CANCEL ORDER
    // =========================================================

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with this email : " + email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        ));
        if(!order.getUser().getUserId().equals(user.getUserId())) throw new UnauthorizedException("User cannot access other users orders");

        // Only PENDING and CONFIRMED orders can be cancelled
        if (order.getOrderStatus() != OrderStatus.PENDING &&
                order.getOrderStatus() != OrderStatus.CONFIRMED) {

            throw new OrderCannotBeCancelled(
                    "Order with id: " + orderId +
                            " cannot be cancelled as it is in " +
                            order.getOrderStatus() +
                            " status."
            );
        }

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(order);

        // Restore inventory
        for (OrderItem item : orderItems) {

            Product product = item.getProduct();

            Inventory inventory =
                    inventoryRepository.findByProduct(product)
                            .orElseThrow(() ->
                                    new InventoryNotFoundException(
                                            "Inventory not found for product: "
                                                    + product.getProdName()
                                    ));

            int quantity = item.getQuantity();

            inventory.setCurrentStock(
                    inventory.getCurrentStock() + quantity
            );

            inventoryRepository.save(inventory);

            // Record stock movement
            StockMovement stockMovement =
                    new StockMovement();

            stockMovement.setInventory(inventory);
            stockMovement.setQuantity(quantity);
            stockMovement.setMovementType(
                    MovementType.ORDER_CANCELLED
            );

            stockMovementRepository.save(stockMovement);
        }

        // Update order status
        order.setOrderStatus(OrderStatus.CANCELLED);

        Order savedOrder =
                orderRepository.save(order);

        return convertToResponse(
                savedOrder,
                orderItems
        );
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatusUpdateRequest request) {

        OrderStatus newStatus = request.getNewStatus();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        ));

        if ((order.getOrderStatus() == OrderStatus.PENDING
                && newStatus == OrderStatus.CONFIRMED)

                || (order.getOrderStatus() == OrderStatus.CONFIRMED
                && newStatus == OrderStatus.PROCESSING)

                || (order.getOrderStatus() == OrderStatus.PROCESSING
                && newStatus == OrderStatus.SHIPPED)

                || (order.getOrderStatus() == OrderStatus.SHIPPED
                && newStatus == OrderStatus.DELIVERED)) {

            order.setOrderStatus(newStatus);

        } else {
            throw new OrderStatusCannotBeUpdatedException(
                    "Order status cannot be updated from "
                            + order.getOrderStatus()
                            + " to "
                            + newStatus
            );
        }
        return convertToResponse(
                order,
                orderItemRepository.findByOrder(order)
        );
    }


    // =========================================================
    // HELPER: VALIDATE STOCK
    // =========================================================

    private void validateStock(
            Inventory inventory,
            int quantity,
            Product product) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        if (quantity > inventory.getCurrentStock()) {

            throw new InsufficientStockException(
                    "Insufficient stock for product: "
                            + product.getProdName()
            );
        }
    }


    // =========================================================
    // HELPER: CREATE ORDER ITEM
    // =========================================================

    private OrderItem createOrderItem(
            Order order,
            Product product,
            int quantity) {

        OrderItem orderItem = new OrderItem();

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);

        // Store price at time of purchase
        orderItem.setPrice(
                product.getProdPrice()
        );

        return orderItem;
    }


    // =========================================================
    // HELPER: CALCULATE ITEM TOTAL
    // =========================================================

    private BigDecimal calculateItemTotal(
            BigDecimal price,
            int quantity) {

        return price.multiply(
                BigDecimal.valueOf(quantity)
        );
    }


    // =========================================================
    // HELPER: REDUCE INVENTORY + RECORD MOVEMENT
    // =========================================================

    private void reduceInventory(
            Inventory inventory,
            int quantity) {

        inventory.setCurrentStock(
                inventory.getCurrentStock() - quantity
        );

        inventoryRepository.save(inventory);

        StockMovement stockMovement =
                new StockMovement();

        stockMovement.setInventory(inventory);
        stockMovement.setQuantity(quantity);
        stockMovement.setMovementType(
                MovementType.ORDER
        );

        stockMovementRepository.save(stockMovement);
    }


    // =========================================================
    // CONVERT ORDER TO RESPONSE
    // =========================================================

    private OrderResponse convertToResponse(
            Order order,
            List<OrderItem> orderItems) {

        OrderResponse response =
                new OrderResponse();

        response.setOrderId(
                order.getOrderId()
        );

        response.setUserId(
                order.getUser().getUserId()
        );

        response.setOrderStatus(
                order.getOrderStatus()
        );

        response.setTotalAmount(
                order.getTotalAmount()
        );

        response.setOrderCreatedAt(
                order.getOrderCreatedAt()
        );

        response.setOrderUpdatedAt(
                order.getOrderUpdatedAt()
        );

        List<OrderItemResponse> itemResponses =
                new ArrayList<>();

        for (OrderItem item : orderItems) {

            OrderItemResponse itemResponse =
                    new OrderItemResponse();

            itemResponse.setOrderItemId(
                    item.getOrderItemId()
            );

            itemResponse.setProductId(
                    item.getProduct().getProdId()
            );

            itemResponse.setQuantity(
                    item.getQuantity()
            );

            itemResponse.setPrice(
                    item.getPrice()
            );

            itemResponses.add(itemResponse);
        }

        response.setOrderItems(
                itemResponses
        );

        return response;
    }
}