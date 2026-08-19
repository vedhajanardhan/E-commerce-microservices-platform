package com.ecommerce.cart.controller;

import com.ecommerce.cart.config.SecurityConfig;
import com.ecommerce.cart.dto.request.AddItemRequest;
import com.ecommerce.cart.dto.response.CartResponse;
import com.ecommerce.cart.security.CurrentUserProvider;
import com.ecommerce.cart.security.JwtAuthenticationEntryPoint;
import com.ecommerce.cart.security.JwtAuthenticationFilter;
import com.ecommerce.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CartService cartService;
    @MockBean private CurrentUserProvider currentUserProvider;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser
    void getCart_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        when(currentUserProvider.getUserId(any(Authentication.class))).thenReturn(userId);
        when(cartService.getCart(userId)).thenReturn(
                new CartResponse(userId, List.of(), 0, BigDecimal.ZERO, LocalDateTime.now()));

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk());
    }

    @Test
    void getCart_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void addItem_withValidPayload_returnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(currentUserProvider.getUserId(any(Authentication.class))).thenReturn(userId);
        when(cartService.addItem(any(), any())).thenReturn(
                new CartResponse(userId, List.of(), 1, BigDecimal.TEN, LocalDateTime.now()));

        AddItemRequest request = new AddItemRequest(productId, 1);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void addItem_withZeroQuantity_returns400() throws Exception {
        AddItemRequest request = new AddItemRequest(UUID.randomUUID(), 0);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
