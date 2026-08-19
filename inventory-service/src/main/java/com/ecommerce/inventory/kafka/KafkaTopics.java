package com.ecommerce.inventory.kafka;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String ORDER_CREATED = "order-created";
    public static final String ORDER_CANCELLED = "order-cancelled";
    public static final String INVENTORY_UPDATED = "inventory-updated";
    public static final String STOCK_RESERVATION_FAILED = "stock-reservation-failed";
}
