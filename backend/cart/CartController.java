package com.urbanthreads.backend.cart;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public String addToCart(Authentication authentication,
                            @RequestParam Long productId,
                            @RequestParam int quantity) {

        String email = authentication.getName();

        cartService.addToCart(email, productId, quantity);

        return "Added to cart";
    }
    @GetMapping
    public List<CartResponse> getCart(Authentication authentication) {

        String email = authentication.getName();

        return cartService.getUserCart(email);
    }
    @DeleteMapping("/{cartItemId}")
    public String removeItem(@PathVariable Long cartItemId) {

        cartService.removeFromCart(cartItemId);

        return "Item removed";
    }
    @PutMapping("/{cartItemId}")
    public String updateQuantity(@PathVariable Long cartItemId,
                                 @RequestParam int quantity) {

        cartService.updateQuantity(cartItemId, quantity);

        return "Quantity updated";
    }
}