package com.commerce.flashsale.message.listener;

import com.commerce.flashsale.message.KafkaConfig;
import com.commerce.flashsale.message.OrderEvent;
import com.commerce.flashsale.service.OrderHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderHistoryService orderHistoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.TOPIC_NAME,
        groupId = KafkaConfig.LISTENER_NAME,
        containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message, Acknowledgment acknowledgment) {
        try{
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("메시지 수신: {}", message);
            orderHistoryService.recordOrderHistory(event.uuid(), event.success());
            acknowledgment.acknowledge();
        } catch (Exception exception) {
            throw new RuntimeException("메시지 처리 중 오류 발생: " + exception.getMessage(), exception);
        }
    }
}
