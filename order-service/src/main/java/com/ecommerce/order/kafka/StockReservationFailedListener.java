package com.ecommerce.order.kafka;

import com.ecommerce.order.event.StockReservationFailedEvent;
import com.ecommerce.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StockReservationFailedListener {

    private final OrderService orderService;

    public StockReservationFailedListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RESERVATION_FAILED, groupId = "order-service")
    public void onStockReservationFailed(StockReservationFailedEvent event) {
        log.info("Received StockReservationFailedEvent: orderId={}", event.orderId());
        orderService.handleStockReservationFailed(event);
    }
}
