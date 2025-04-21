package com.commerce.flashsale.service;

import com.commerce.flashsale.message.producer.OrderEventProducer;
import com.commerce.flashsale.repository.Order;
import com.commerce.flashsale.repository.OrderRepository;
import com.commerce.flashsale.repository.RedisRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ValidationService validationService;
    private final RedisRepository redisRepository;
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public boolean create(String uuid) {
        boolean validate = validationService.validate(uuid);
        if (!validate) {
            log.error("주문 검증 단계 실패 - UUID: {}", uuid);
            orderEventProducer.produceMessage(uuid, false);
            return false;
        }

        boolean reduced = redisRepository.reduceOrderCount();
        if (!reduced) {
            log.error("주문 수량 부족 - UUID: {}", uuid);
            orderEventProducer.produceMessage(uuid, false);
            return false;
        }

        orderEventProducer.produceMessage(uuid, true);
        return true;
    }

    @Transactional
    public void createOrder(String uuid, boolean success) {
        Order order = Order.builder()
            .uuid(uuid)
            .success(success)
            .build();

        orderRepository.save(order);
        log.info("주문 상태 업데이트 완료");
    }
}
