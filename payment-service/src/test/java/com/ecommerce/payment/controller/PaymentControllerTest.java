package com.ecommerce.payment.controller;

import com.ecommerce.payment.config.SecurityConfig;
import com.ecommerce.payment.dto.request.ChargeRequest;
import com.ecommerce.payment.dto.response.PaymentResult;
import com.ecommerce.payment.security.JwtAuthenticationEntryPoint;
import com.ecommerce.payment.security.JwtAuthenticationFilter;
import com.ecommerce.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PaymentService paymentService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser
    void charge_withValidPayload_returns200() throws Exception {
        ChargeRequest request = new ChargeRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50.00"));
        when(paymentService.charge(any())).thenReturn(new PaymentResult("SUCCESS", "txn_1", "ok"));

        mockMvc.perform(post("/api/payments/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void charge_withoutAuth_returns401() throws Exception {
        ChargeRequest request = new ChargeRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("50.00"));

        mockMvc.perform(post("/api/payments/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void charge_withNegativeAmount_returns400() throws Exception {
        ChargeRequest request = new ChargeRequest(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/payments/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllPaymentsAdmin_asNonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/payments/admin"))
                .andExpect(status().isForbidden());
    }
}
