package com.ecommerce.order.controller;

import com.ecommerce.order.config.SecurityConfig;
import com.ecommerce.order.dto.request.PlaceOrderRequest;
import com.ecommerce.order.dto.response.OrderResponse;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.security.CurrentUserProvider;
import com.ecommerce.order.security.JwtAuthenticationEntryPoint;
import com.ecommerce.order.security.JwtAuthenticationFilter;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private CurrentUserProvider currentUserProvider;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private OrderResponse sampleOrder() {
        return new OrderResponse(UUID.randomUUID(), UUID.randomUUID(), OrderStatus.CONFIRMED,
                new BigDecimal("100.00"), "123 Main St", "txn-1", null, List.of(), LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void placeOrder_withValidPayload_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        when(currentUserProvider.getUserId(any(Authentication.class))).thenReturn(userId);
        when(orderService.placeOrder(any(), any())).thenReturn(sampleOrder());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PlaceOrderRequest("123 Main St"))))
                .andExpect(status().isCreated());
    }

    @Test
    void placeOrder_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PlaceOrderRequest("123 Main St"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void placeOrder_withBlankAddress_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PlaceOrderRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getMyOrders_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        when(currentUserProvider.getUserId(any(Authentication.class))).thenReturn(userId);
        when(orderService.getOrderHistory(any(), any())).thenReturn(new PageImpl<>(List.of(sampleOrder())));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllOrdersAdmin_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/orders/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrdersAdmin_asAdmin_returns200() throws Exception {
        when(orderService.getAllOrders(any(), any())).thenReturn(new PageImpl<>(List.of(sampleOrder())));

        mockMvc.perform(get("/api/orders/admin"))
                .andExpect(status().isOk());
    }
}
