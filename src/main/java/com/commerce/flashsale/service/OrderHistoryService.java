package com.commerce.flashsale.service;

import com.commerce.flashsale.repository.OrderHistory;
import com.commerce.flashsale.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;

    @Transactional
    public void recordOrderHistory(String uuid, boolean success, String productName) {
        OrderHistory order = OrderHistory.builder()
            .uuid(uuid)
            .productName(productName)
            .success(success)
            .build();

        orderHistoryRepository.save(order);
        log.info("주문 상태 업데이트 완료");
    }

    @Transactional(readOnly = true)
    public boolean hasSuccessHistory(String uuid) {
        return orderHistoryRepository.hasSuccessHistory(uuid);
    }

    @Transactional(readOnly = true)
    public Integer getOrderSuccessCount(String productName) {
        return orderHistoryRepository.countBySuccessIsTrueAndProductName(productName);
    }

    @Transactional
    public void deleteAllOrderHistory() {
        orderHistoryRepository.deleteAll();
    }
}
