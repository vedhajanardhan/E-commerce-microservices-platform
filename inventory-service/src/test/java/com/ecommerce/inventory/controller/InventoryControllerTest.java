package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.config.SecurityConfig;
import com.ecommerce.inventory.dto.request.StockValidationRequest;
import com.ecommerce.inventory.dto.response.InventoryResponse;
import com.ecommerce.inventory.dto.response.StockValidationResponse;
import com.ecommerce.inventory.security.JwtAuthenticationEntryPoint;
import com.ecommerce.inventory.security.JwtAuthenticationFilter;
import com.ecommerce.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser(roles = "USER")
    void getInventory_asAuthenticatedUser_returns200() throws Exception {

        UUID productId = UUID.randomUUID();

        when(inventoryService.getInventory(productId)).thenReturn(
                new InventoryResponse(
                        productId,
                        "SKU-1",
                        10,
                        5,
                        false,
                        LocalDateTime.now()
                )
        );

        mockMvc.perform(
                get("/api/inventory/{productId}", productId)
        ).andExpect(status().isOk());
    }

    @Test
    void getInventory_withoutAuth_returns401() throws Exception {

        mockMvc.perform(
                get("/api/inventory/{productId}", UUID.randomUUID())
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void increase_asNonAdmin_returns403() throws Exception {

        mockMvc.perform(
                post("/api/inventory/{productId}/increase", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5,\"reason\":\"restock\"}")
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void increase_asAdmin_returns200() throws Exception {

        UUID productId = UUID.randomUUID();

        when(
                inventoryService.increaseStock(
                        any(),
                        any(Integer.class),
                        any(),
                        any()
                )
        ).thenReturn(
                new InventoryResponse(
                        productId,
                        "SKU-1",
                        15,
                        5,
                        false,
                        LocalDateTime.now()
                )
        );

        mockMvc.perform(
                post("/api/inventory/{productId}/increase", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5,\"reason\":\"restock\"}")
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void validate_asAuthenticatedUser_returns200() throws Exception {

        // CHANGED: valid() -> success()
        when(inventoryService.validateStock(any()))
                .thenReturn(StockValidationResponse.success());

        var request = new StockValidationRequest(
                List.of(
                        new StockValidationRequest.StockCheckItem(
                                UUID.randomUUID(),
                                2
                        )
                )
        );

        mockMvc.perform(
                post("/api/inventory/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk());
    }
}