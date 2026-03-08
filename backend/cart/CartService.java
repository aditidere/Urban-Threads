package com.urbanthreads.backend.cart;

import com.urbanthreads.backend.product.Product;
import com.urbanthreads.backend.product.ProductRepository;
import com.urbanthreads.backend.user.User;
import com.urbanthreads.backend.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public void addToCart(String email, Long productId, int quantity) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        Product product = productRepository.findById(productId)
                .orElseThrow();

        CartItem cartItem = new CartItem();

        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        cartRepository.save(cartItem);
    }
    public List<CartResponse> getUserCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        List<CartItem> items = cartRepository.findByUserId(user.getId());

        return items.stream().map(item -> {

            CartResponse response = new CartResponse();

            response.setCartItemId(item.getId());
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
            response.setPrice(item.getProduct().getPrice());
            response.setImageUrl(item.getProduct().getImageUrl());
            response.setQuantity(item.getQuantity());

            return response;

        }).toList();
    }
    public void removeFromCart(Long cartItemId) {

        cartRepository.deleteById(cartItemId);
    }
    public void updateQuantity(Long cartItemId, int quantity) {

        CartItem item = cartRepository.findById(cartItemId)
                .orElseThrow();

        item.setQuantity(quantity);

        cartRepository.save(item);
    }
}