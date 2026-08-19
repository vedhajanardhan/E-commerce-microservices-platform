package com.ecommerce.payment.config;

import com.ecommerce.payment.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentProcessedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_PROCESSED).partitions(3).replicas(1).build();
    }
}
