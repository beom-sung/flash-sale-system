package com.commerce.flashsale.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor
public class OrderHistory {

    @GeneratedValue(strategy = IDENTITY)
    @Id
    private Long id;
    private String uuid;
    private Boolean success;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public OrderHistory(String uuid, Boolean success) {
        this.uuid = uuid;
        this.success = success;
    }
}
