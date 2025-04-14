package com.commerce.flashsale.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "orders")
@NoArgsConstructor
public class Order {

    @GeneratedValue(strategy = IDENTITY)
    @Id
    private Long id;
    private String uuid;
    private Boolean success;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public Order(String uuid, Boolean success) {
        this.uuid = uuid;
        this.success = success;
    }
}
