package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.event.OrderEvent;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "product-stock-deducted", groupId = "order-group")
    @Transactional
    public void handleStockDeducted(OrderEvent event) {
        log.info("재고 차감 완료 이벤트 수신: {}", event);
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        order.setStatus("COMPLETED");
        orderRepository.save(order);
    }

    @KafkaListener(topics = "product-stock-failed", groupId = "order-group")
    @Transactional
    public void handleStockFailed(OrderEvent event) {
        log.info("재고 차감 실패 이벤트 수신 (보상 트랜잭션): {}", event);
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }
}
