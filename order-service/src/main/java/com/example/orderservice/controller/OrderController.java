package com.example.orderservice.controller;

import com.example.orderservice.entity.Order;
import com.example.orderservice.event.OrderEvent;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        // 1. 주문을 PENDING 상태로 저장
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // 2. Kafka에 주문 생성 이벤트 발행
        OrderEvent event = new OrderEvent(
                savedOrder.getId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                "PENDING"
        );
        kafkaTemplate.send("order-create", event);

        return savedOrder;
    }
}
