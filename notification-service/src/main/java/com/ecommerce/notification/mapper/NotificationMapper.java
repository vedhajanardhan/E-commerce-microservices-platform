package com.ecommerce.notification.mapper;

import com.ecommerce.notification.dto.response.NotificationResponse;
import com.ecommerce.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toNotificationResponse(Notification notification);
}
