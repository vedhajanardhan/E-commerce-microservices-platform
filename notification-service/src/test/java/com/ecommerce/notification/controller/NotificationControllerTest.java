package com.ecommerce.notification.controller;

import com.ecommerce.notification.config.SecurityConfig;
import com.ecommerce.notification.security.CurrentUserProvider;
import com.ecommerce.notification.security.JwtAuthenticationEntryPoint;
import com.ecommerce.notification.security.JwtAuthenticationFilter;
import com.ecommerce.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private NotificationService notificationService;
    @MockBean private CurrentUserProvider currentUserProvider;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser
    void getMyNotifications_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        when(currentUserProvider.getUserId(any(Authentication.class))).thenReturn(userId);
        when(notificationService.getNotificationsForUser(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/notifications/me"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyNotifications_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/notifications/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllNotificationsAdmin_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/notifications/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllNotificationsAdmin_asAdmin_returns200() throws Exception {
        when(notificationService.getAllNotifications(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/notifications/admin"))
                .andExpect(status().isOk());
    }
}
