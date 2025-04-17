package com.commerce.flashsale.listner;

import com.commerce.flashsale.message.KafkaConfig;
import com.commerce.flashsale.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = KafkaConfig.TOPIC_NAME,
        groupId = KafkaConfig.LISTENER_NAME,
        containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message, Acknowledgment acknowledgment) {
        log.info("메시지 수신: {}", message);
        orderService.createOrder(UUID.randomUUID().toString(), Boolean.parseBoolean(message));
        acknowledgment.acknowledge();
    }
}
