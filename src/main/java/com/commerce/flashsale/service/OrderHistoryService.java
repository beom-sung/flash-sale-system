package com.commerce.flashsale.service;

import com.commerce.flashsale.repository.OrderHistory;
import com.commerce.flashsale.repository.OrderHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional
    public void recordOrderHistory(String uuid, boolean success) {
        OrderHistory order = OrderHistory.builder()
            .uuid(uuid)
            .success(success)
            .build();

        orderHistoryRepository.save(order);
        log.info("주문 상태 업데이트 완료");
    }
}
