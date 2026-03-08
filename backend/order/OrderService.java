package com.urbanthreads.backend.order;

import com.urbanthreads.backend.cart.CartItem;
import com.urbanthreads.backend.cart.CartRepository;
import com.urbanthreads.backend.user.User;
import com.urbanthreads.backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // CHECKOUT
    // =====================================================
    public Order checkout(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CartItem> cartItems =
                cartRepository.findByUserId(user.getId());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);

        List<OrderItem> orderItems = cartItems.stream().map(cart -> {

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(cart.getProduct());
            item.setQuantity(cart.getQuantity());
            item.setPriceAtPurchase(cart.getProduct().getPrice());

            return item;

        }).toList();

        double total = orderItems.stream()
                .mapToDouble(i ->
                        i.getPriceAtPurchase() * i.getQuantity())
                .sum();

        order.setItems(orderItems);
        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteAll(cartItems);

        return savedOrder;
    }

    // =====================================================
    // GET USER ORDERS
    // =====================================================
    public List<Order> getUserOrders(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUserId(user.getId());
    }

    public Order updateStatus(Long orderId,
                              OrderStatus newStatus) {

        // 1️⃣ Fetch order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        // 2️⃣ Fix old NULL statuses
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PLACED);
        }

        OrderStatus currentStatus = order.getStatus();

        // 3️⃣ Block finalized orders
        if (currentStatus == OrderStatus.DELIVERED ||
                currentStatus == OrderStatus.CANCELLED) {

            throw new RuntimeException(
                    "This order is already finalized and cannot change status");
        }

        // 4️⃣ Validate flow
        validateStatusTransition(currentStatus, newStatus);

        // 5️⃣ Update
        order.setStatus(newStatus);

        return orderRepository.save(order);
    }

    // =====================================================
    // STATUS FLOW VALIDATION
    // =====================================================
    private void validateStatusTransition(
            OrderStatus current,
            OrderStatus next) {

        switch (current) {

            case PLACED:
                if (next != OrderStatus.PAYMENT_PENDING)
                    throw new RuntimeException(
                            "From PLACED → PAYMENT_PENDING only");
                break;

            case PAYMENT_PENDING:
                if (next != OrderStatus.PAID &&
                        next != OrderStatus.PAYMENT_FAILED)
                    throw new RuntimeException(
                            "From PAYMENT_PENDING → PAID or PAYMENT_FAILED");
                break;

            case PAID:
                if (next != OrderStatus.PROCESSING)
                    throw new RuntimeException(
                            "From PAID → PROCESSING only");
                break;

            case PROCESSING:
                if (next != OrderStatus.SHIPPED)
                    throw new RuntimeException(
                            "From PROCESSING → SHIPPED only");
                break;

            case SHIPPED:
                if (next != OrderStatus.DELIVERED)
                    throw new RuntimeException(
                            "From SHIPPED → DELIVERED only");
                break;

            default:
                throw new RuntimeException("Invalid status transition");
        }
    }
    // =====================================================
// GET ORDER STATUS (FOR AUTH USER)
// =====================================================
    public OrderStatus getOrderStatus(Long orderId, String email) {

        // 1️⃣ Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2️⃣ Find order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 3️⃣ Security check
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        // 4️⃣ Fix old orders (status null issue)
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PLACED);
        }

        return order.getStatus();
    }
}