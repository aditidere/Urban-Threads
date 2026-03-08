package com.urbanthreads.backend.payment;

import com.urbanthreads.backend.dto.PaymentVerifyRequest;
import com.urbanthreads.backend.order.Order;
import com.urbanthreads.backend.order.OrderItem;
import com.urbanthreads.backend.order.OrderRepository;
import com.urbanthreads.backend.order.OrderService;
import com.urbanthreads.backend.order.OrderStatus;
import com.urbanthreads.backend.product.Product;
import com.urbanthreads.backend.product.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    public PaymentService(OrderRepository orderRepository,
                          ProductRepository productRepository,
                          OrderService orderService) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderService = orderService;

    }

    // =====================================
    // PAYMENT SIMULATION
    // =====================================
    public Order simulatePayment(Long orderId, boolean success) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // STEP 1 — PAYMENT STARTED
        orderService.updateStatus(orderId, OrderStatus.PAYMENT_PENDING);

        if (success) {

            // STEP 2 — UPDATE STOCK
            for (OrderItem item : order.getItems()) {

                Product product = item.getProduct();

                int updatedStock =
                        product.getStock() - item.getQuantity();

                if (updatedStock < 0) {
                    throw new RuntimeException(
                            "Not enough stock for product: "
                                    + product.getName());
                }

                product.setStock(updatedStock);
                productRepository.save(product);
            }

            // STEP 3 — PAYMENT SUCCESS
            orderService.updateStatus(orderId, OrderStatus.PAID);

            // STEP 4 — AUTO MOVE TO PROCESSING
            return orderService.updateStatus(orderId,
                    OrderStatus.PROCESSING);

        } else {

            // STEP 5 — PAYMENT FAILED
            return orderService.updateStatus(orderId,
                    OrderStatus.PAYMENT_FAILED);
        }
    }
    public Order verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) {

        Order order = orderRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update stock
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            int updatedStock = product.getStock() - item.getQuantity();

            if (updatedStock < 0) {
                throw new RuntimeException(
                        "Not enough stock for product: " + product.getName());
            }

            product.setStock(updatedStock);
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.PAID);

        return orderRepository.save(order);
    }
}