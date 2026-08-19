package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.response.OrderItemResponse;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    default OrderItemResponse toOrderItemResponse(OrderItem item) {
        if (item == null) {
            return null;
        }
        return new OrderItemResponse(
                item.getProductId(), item.getSku(), item.getProductName(),
                item.getUnitPrice(), item.getQuantity(), item.getSubtotal());
    }

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    default OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getPaymentTransactionId(),
                order.getCancellationReason(),
                toOrderItemResponseList(order.getItems()),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
