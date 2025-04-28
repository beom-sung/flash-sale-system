package com.commerce.flashsale.message.producer;

import com.commerce.flashsale.message.KafkaConfig;
import com.commerce.flashsale.message.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void produceMessage(String uuid, String productName, boolean success) {
        try {
            log.info("카프카 메시지 발행 시작 - 토픽: {}, 메시지: {}", KafkaConfig.TOPIC_NAME, success);
            OrderEvent event = OrderEvent.builder()
                .uuid(uuid)
                .productName(productName)
                .success(success)
                .build();
            kafkaTemplate.send(KafkaConfig.TOPIC_NAME, event)
                .thenAccept(stringStringSendResult -> {
                    log.info("카프카 메시지 발행 성공 - 결과: {}, 토픽: {}, 파티션: {}, 오프셋: {}",
                        stringStringSendResult,
                        stringStringSendResult.getRecordMetadata().topic(),
                        stringStringSendResult.getRecordMetadata().partition(),
                        stringStringSendResult.getRecordMetadata().offset());
                });
        } catch (Exception e) {
            log.error("카프카 메시지 발행 실패 - 예외: {}", e.getMessage(), e);
            throw new RuntimeException("카프카 메시지 발행 중 오류 발생: " + e.getMessage(), e);
        }
    }
}
