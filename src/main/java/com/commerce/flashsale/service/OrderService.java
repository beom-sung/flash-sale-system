package com.commerce.flashsale.service;

import com.commerce.flashsale.message.KafkaConfig;
import com.commerce.flashsale.repository.Order;
import com.commerce.flashsale.repository.OrderRepository;
import com.commerce.flashsale.repository.RedisRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ValidationService validationService;
    private final RedisRepository redisRepository;
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public boolean create(String uuid) {
        boolean validate = validationService.validate(uuid); // 성공 or 실패
        redisRepository.save(uuid, validate); // 재고 count 처리
        produceMessage(uuid, validate);
        return validate;
    }

    public void produceMessage(String uuid, boolean success) {
        try {
            log.info("카프카 메시지 발행 시작 - 토픽: {}, 메시지: {}", KafkaConfig.TOPIC_NAME, success);
            OrderEvent event = OrderEvent.builder()
                .uuid(uuid)
                .success(success)
                .build();
            SendResult<String, Object> stringStringSendResult = kafkaTemplate.send(KafkaConfig.TOPIC_NAME, event)
                .get();
            log.info("카프카 메시지 발행 성공 - 결과: {}, 토픽: {}, 파티션: {}, 오프셋: {}",
                stringStringSendResult,
                stringStringSendResult.getRecordMetadata().topic(),
                stringStringSendResult.getRecordMetadata().partition(),
                stringStringSendResult.getRecordMetadata().offset());
        } catch (Exception e) {
            log.error("카프카 메시지 발행 실패 - 예외: {}", e.getMessage(), e);
            throw new RuntimeException("카프카 메시지 발행 중 오류 발생: " + e.getMessage(), e);
        }
    }


    @Transactional
    public void createOrder(String uuid, boolean success) {
        // 주문 상태 업데이트 로직
        Order order = Order.builder()
            .uuid(uuid)
            .success(success)
            .build();

        orderRepository.save(order);
        log.info("주문 상태 업데이트 완료");
    }
}
