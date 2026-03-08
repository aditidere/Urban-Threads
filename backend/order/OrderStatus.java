package com.urbanthreads.backend.order;

public enum OrderStatus {

    PLACED,
    PAYMENT_PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    PAYMENT_FAILED
}