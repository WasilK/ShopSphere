package com.wasil.ShopSphere.scheduler;

import com.wasil.ShopSphere.services.OrderService;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

public class OrderExpiryScheduler {
    private final OrderService orderService;

    public OrderExpiryScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    // Runs every 5 minutes; cancels any order still PENDING after 30 minutes.
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void expireAbandonedOrders() {
        orderService.expireStalePendingOrders(Duration.ofMinutes(30));
    }
}
