package com.urbanthreads.backend.payment;

import com.razorpay.RazorpayException;
import com.urbanthreads.backend.order.Order;
import com.urbanthreads.backend.order.OrderRepository;
import com.urbanthreads.backend.order.OrderService;
import org.springframework.web.bind.annotation.*;
import com.urbanthreads.backend.dto.PaymentVerifyRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final RazorpayService razorpayService;

    public PaymentController(PaymentService paymentService, OrderService orderService, OrderRepository orderRepository, RazorpayService razorpayService) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;

        this.razorpayService = razorpayService;
    }

    @PostMapping("/{orderId}")
    public Order pay(@PathVariable Long orderId,
                     @RequestParam boolean success) {

        return paymentService.simulatePayment(orderId, success);
    }
    @PostMapping("/create-order/{orderId}")
    public Map<String, Object> createOrder(@PathVariable Long orderId) throws RazorpayException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        com.razorpay.Order razorpayOrder =
                razorpayService.createRazorpayOrder(order.getTotalPrice());

        order.setRazorpayOrderId(razorpayOrder.get("id"));
        orderRepository.save(order);

        Map<String, Object> response = new HashMap<>();

        response.put("razorpayOrderId", razorpayOrder.get("id"));
        response.put("amount", razorpayOrder.get("amount"));

        return response;
    }
    @PostMapping("/verify")
    public Order verifyPayment(@RequestBody PaymentVerifyRequest request) {

        return paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );
    }
}