package com.ecommerce.order.config;

import com.ecommerce.order.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * order-created and stock-reservation-failed are already declared as
 * NewTopic beans by inventory-service; either service's KafkaAdmin can
 * create a topic that doesn't exist yet, so declaring order-cancelled
 * here (the topic order-service itself owns/produces) is sufficient —
 * no conflict from both services separately declaring the shared topics
 * with identical partition/replica settings.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CREATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_CANCELLED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic stockReservationFailedTopic() {
        return TopicBuilder.name(KafkaTopics.STOCK_RESERVATION_FAILED).partitions(3).replicas(1).build();
    }
}
