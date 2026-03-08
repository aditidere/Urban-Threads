package com.urbanthreads.backend.cart;

import com.urbanthreads.backend.user.User;
import com.urbanthreads.backend.product.Product;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Product product;

    private int quantity;
}