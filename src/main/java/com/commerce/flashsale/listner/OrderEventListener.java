package com.commerce.flashsale.listner;

import com.commerce.flashsale.message.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventListener {

    @KafkaListener(topics = KafkaConfig.TOPIC_NAME,
        groupId = KafkaConfig.LISTENER_NAME,
        containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message, Acknowledgment acknowledgment) {
        log.info("메시지 수신: {}", message);
        acknowledgment.acknowledge();
    }
}
