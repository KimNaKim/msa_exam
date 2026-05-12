package com.example.productservice.service;

import com.example.productservice.entity.Product;
import com.example.productservice.event.OrderEvent;
import com.example.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-create", groupId = "product-group")
    @Transactional
    public void handleOrderCreated(OrderEvent event) {
        log.info("수신된 주문 이벤트: {}", event);
        try {
            Product product = productRepository.findById(event.getProductId())
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
            
            // 재고 차감 시도
            product.reduceStock(event.getQuantity());
            productRepository.save(product);

            // 성공 이벤트 발행
            event.setStatus("COMPLETED");
            kafkaTemplate.send("product-stock-deducted", event);
            log.info("재고 차감 성공: 주문 ID {}", event.getOrderId());
            
        } catch (Exception e) {
            log.error("재고 차감 실패: {}", e.getMessage());
            // 실패 이벤트 발행 (보상 트랜잭션 트리거)
            event.setStatus("CANCELLED");
            kafkaTemplate.send("product-stock-failed", event);
        }
    }
}
