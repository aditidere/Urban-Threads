package com.urbanthreads.backend.dto;

import com.urbanthreads.backend.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusResponse {

    private Long orderId;
    private OrderStatus status;
}