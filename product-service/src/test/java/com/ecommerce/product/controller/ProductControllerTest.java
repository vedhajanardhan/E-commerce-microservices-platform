package com.ecommerce.product.controller;

import com.ecommerce.product.config.SecurityConfig;
import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.security.JwtAuthenticationEntryPoint;
import com.ecommerce.product.security.JwtAuthenticationFilter;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
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

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProductService productService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private ProductResponse sampleProduct() {
        return new ProductResponse(UUID.randomUUID(), "SKU-1", "Test Product", "desc",
                new BigDecimal("100.00"), 1L, "Electronics", "Acme", true,
                List.of(), LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "USER")
    void search_asAuthenticatedUser_returns200() throws Exception {
        Page<ProductResponse> page = new PageImpl<>(List.of(sampleProduct()));
        when(productService.searchProducts(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void search_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_asAdmin_returns201() throws Exception {
        ProductRequest request = new ProductRequest("SKU-1", "Test Product", "desc",
                new BigDecimal("100.00"), 1L, "Acme", true, List.of());
        when(productService.createProduct(any())).thenReturn(sampleProduct());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_asNonAdmin_returns403() throws Exception {
        ProductRequest request = new ProductRequest("SKU-1", "Test Product", "desc",
                new BigDecimal("100.00"), 1L, "Acme", true, List.of());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
