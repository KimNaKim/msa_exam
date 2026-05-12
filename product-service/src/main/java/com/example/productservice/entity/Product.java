package com.example.productservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer price;
    private Integer stockQuantity;

    public void reduceStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new RuntimeException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
    }

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
