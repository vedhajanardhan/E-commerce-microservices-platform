package com.ecommerce.inventory.config;

import com.ecommerce.inventory.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Spring Kafka's KafkaAdmin auto-creates any NewTopic beans found in the
 * context on startup (as long as the broker allows auto-creation), so
 * local/dev environments don't need a separate topic-provisioning step.
 * 3 partitions gives some parallelism headroom without over-provisioning
 * for a single-broker dev Kafka.
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
    public NewTopic inventoryUpdatedTopic() {
        return TopicBuilder.name(KafkaTopics.INVENTORY_UPDATED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic stockReservationFailedTopic() {
        return TopicBuilder.name(KafkaTopics.STOCK_RESERVATION_FAILED).partitions(3).replicas(1).build();
    }
}
