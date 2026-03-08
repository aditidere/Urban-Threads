package com.urbanthreads.backend.order;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ==============================
    // CHECKOUT API
    // ==============================
    @PostMapping("/checkout")
    public Order checkout(Authentication authentication) {

        String email = authentication.getName();

        return orderService.checkout(email);
    }

    // ==============================
    // GET MY ORDERS
    // ==============================
    @GetMapping("/my-orders")
    public List<Order> getMyOrders(Authentication authentication) {

        String email = authentication.getName();

        return orderService.getUserOrders(email);
    }
    @PutMapping("/{id}/status")
    public Order updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        return orderService.updateStatus(id, status);
    }
    @GetMapping("/{id}/status")
    public OrderStatus getStatus(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        return orderService.getOrderStatus(id, email);
    }
}