package com.urbanthreads.backend.cart;

import lombok.Data;

@Data
public class CartResponse {

    private Long cartItemId;
    private Long productId;
    private String productName;
    private double price;
    private String imageUrl;
    private int quantity;
}