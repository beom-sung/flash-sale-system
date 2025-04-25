package com.commerce.flashsale.service;

import com.commerce.flashsale.message.producer.OrderEventProducer;
import com.commerce.flashsale.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ValidationService validationService;
    private final RedisRepository redisRepository;
    private final OrderEventProducer orderEventProducer;

    public boolean create(String uuid, String productName) {
        boolean validate = validationService.validate(uuid);
        if (!validate) {
            log.error("주문 검증 단계 실패 - UUID: {}", uuid);
            orderEventProducer.produceMessage(uuid, productName, false);
            return false;
        }

        boolean reduced = redisRepository.reduceStockCount(uuid);
        if (!reduced) {
            log.error("주문 수량 부족 - UUID: {}", uuid);
            orderEventProducer.produceMessage(uuid, productName, false);
            return false;
        }

        orderEventProducer.produceMessage(uuid, productName, true);
        return true;
    }

    public void createProduct(String productName, int stockCount) {
        redisRepository.setProduct(productName, stockCount);
    }
}
