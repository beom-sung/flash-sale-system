package com.commerce.flashsale.service;

import com.commerce.flashsale.message.KafkaConfig;
import com.commerce.flashsale.repository.Order;
import com.commerce.flashsale.repository.OrderRepository;
import com.commerce.flashsale.repository.RedisRepository;
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
    private final KafkaTemplate<String, String> kafkaTemplate;

    public boolean create(String uuid) {
        boolean validate = validationService.validate(uuid); // 성공 or 실패
        redisRepository.save(uuid, validate); // 재고 count 처리
        orderRepository.save(Order.builder().uuid(uuid).success(validate).build()); // 구매 히스토리 저장
        produceMessage(validate);
        return validate;
    }

    public void produceMessage(boolean validate) {
        try {
            log.info("카프카 메시지 발행 시작 - 토픽: {}, 메시지: {}", KafkaConfig.TOPIC_NAME, validate);
            SendResult<String, String> stringStringSendResult = kafkaTemplate.send(KafkaConfig.TOPIC_NAME, String.valueOf(validate))
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
}
