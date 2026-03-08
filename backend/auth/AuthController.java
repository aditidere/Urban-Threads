package com.urbanthreads.backend.auth;

//import com.urbanthreads.backend.auth.dto.RegisterRequest;
import com.urbanthreads.backend.dto.LoginRequest;
import com.urbanthreads.backend.dto.OrderStatusResponse;
import com.urbanthreads.backend.dto.RegisterRequest;
import com.urbanthreads.backend.order.Order;
import com.urbanthreads.backend.order.OrderRepository;
import com.urbanthreads.backend.order.OrderService;
import com.urbanthreads.backend.order.OrderStatus;
import com.urbanthreads.backend.user.User;
import com.urbanthreads.backend.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/auth")

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final UserRepository userRepository;
    private  final OrderService orderService;
    private final OrderRepository orderRepository;

    public AuthController(PasswordEncoder passwordEncoder, AuthService authService,
                          UserRepository userRepository, OrderService orderService, OrderRepository orderRepository) {
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.userRepository = userRepository;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return Map.of("message", "User registered successfully");
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return Map.of("token", token);
    }

    @GetMapping("/me")
    public User getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @GetMapping("/test-auth")
    public String testAuth(Authentication authentication) {

        return "Logged in as: " + authentication.getName();
    }
    public OrderStatus getOrderStatus(Long orderId, String email) {

        // find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // find order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // security check (user owns order)
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        // legacy fix
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PLACED);
        }

        return order.getStatus();
    }
    @PostMapping("/google")
    public Map<String, String> googleLogin(@RequestBody GoogleTokenRequest request) {

        String token = authService.googleLogin(request.getToken());

        return Map.of("token", token);
    }
    @GetMapping(value = "/hash", produces = "text/plain")
    public String hashPassword(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
}