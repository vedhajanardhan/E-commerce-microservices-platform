package com.ecommerce.notification.kafka;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order-created";
    public static final String PAYMENT_PROCESSED = "payment-processed";
    public static final String INVENTORY_UPDATED = "inventory-updated";
}
