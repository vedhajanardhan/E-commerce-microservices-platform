package com.ecommerce.notification.kafka;

import com.ecommerce.notification.client.AuthServiceClient;
import com.ecommerce.notification.client.AuthUserInfo;
import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.event.OrderItemEvent;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.notification.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Spins up an in-memory Kafka broker and drives the real publish ->
 * @KafkaListener -> (mock) email -> DB pipeline end to end. The Feign
 * call to auth-service and the actual email send are mocked (this is a
 * Kafka-wiring test, not an auth-service or SMTP integration test).
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.ORDER_CREATED, KafkaTopics.PAYMENT_PROCESSED, KafkaTopics.INVENTORY_UPDATED})
@DirtiesContext
class NotificationEventListenerIntegrationTest {

    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired private NotificationRepository notificationRepository;

    @MockBean private AuthServiceClient authServiceClient;
    @MockBean private EmailSender emailSender;

    @Test
    void publishingOrderCreated_triggersEmailAndPersistsNotification() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(authServiceClient.getUserById(userId)).thenReturn(
                new AuthUserInfo(userId, "vedha", "vedha@example.com", "Vedha", "J"));
        when(emailSender.send(anyString(), anyString(), anyString())).thenReturn(true);

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, userId, List.of(new OrderItemEvent(UUID.randomUUID(), 1)), Instant.now());

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, orderId.toString(), event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            long count = notificationRepository.findAll().stream()
                    .filter(n -> orderId.equals(n.getRelatedEntityId()))
                    .count();
            assertEquals(1, count);
        });
    }
}
