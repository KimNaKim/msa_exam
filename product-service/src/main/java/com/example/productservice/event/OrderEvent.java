package com.example.productservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String status; // PENDING, COMPLETED, CANCELLED
}
