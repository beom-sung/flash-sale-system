package com.commerce.flashsale.message;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_NAME = "order-completed-event";
    public static final String LISTENER_NAME = "order-event-listener";

    private static final String EARLIEST = "earliest";

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(senderProps());
    }

    private Map<String, Object> senderProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:20004");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(ProducerConfig.RETRIES_CONFIG, 2147483647);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 200);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 60000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
        return props;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /*
        Kafka Consumer 설정
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:20004");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, LISTENER_NAME);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        factory.setConcurrency(1);  // 멀티 스레드 컨슈머

        // DeadLetterPublishingRecoverer를 사용하여 처리 실패한 메시지를 DLT로 전송
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, exception) -> {
//                return new TopicPartition(record.topic() + ".DLT", record.partition()); // 원본 토픽에 신규 파티션이 생성되고 신규 파티션에서 DLT로 전송되면 에러가 발생하여 DLT로 전송되지 않는다.
//                return new TopicPartition(record.topic() + ".DLT", 0); // DLT 파티션이 2개 이상이면 0번 파티션에만 이벤트가 쏠리니까 이건 좀 위험하지 않을까?
                return new TopicPartition(record.topic() + ".DLT", -1); // 원본 토픽 파티션을 아무리 늘려도 DLT 파티션은 신경 안써도 된다. 알아서 파티션에 잘 분배 되어 이벤트가 들어간다.
            });

        // 예외 발생 시 사용할 ErrorHandler 설정
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer,
            new FixedBackOff(1000L, 3) // 1초 간격으로 3번 재시도
        );

        // 특정 예외는 재시도하지 않고 바로 DLT로 전송
//        errorHandler.addNotRetryableExceptions(IllegalStateException.class);

        // DLT로 전송시 원본 레코드 커멋을 `자동`으로 하도록 설정
        errorHandler.setAckAfterHandle(true);

        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
