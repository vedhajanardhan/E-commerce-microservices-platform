package com.ecommerce.user.controller;

import com.ecommerce.user.config.SecurityConfig;
import com.ecommerce.user.dto.request.AddressRequest;
import com.ecommerce.user.dto.response.AddressResponse;
import com.ecommerce.user.entity.AddressType;
import com.ecommerce.user.security.CurrentUserProvider;
import com.ecommerce.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.user.security.JwtAuthenticationFilter;
import com.ecommerce.user.service.AddressService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@Import(SecurityConfig.class)
class AddressControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AddressService addressService;
    @MockBean private CurrentUserProvider currentUserProvider;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @WithMockUser
    void list_returnsAddressesForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(currentUserProvider.getUserId(any(Authentication.class))).thenReturn(userId);
        when(addressService.listAddresses(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/users/me/addresses"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void add_withValidPayload_returns201() throws Exception {
        UUID userId = UUID.randomUUID();
        AddressRequest request = new AddressRequest("221B Baker St", null, "Bengaluru", "Karnataka",
                "560001", "India", AddressType.SHIPPING, true);
        when(currentUserProvider.getUserId(any(Authentication.class))).thenReturn(userId);
        when(addressService.addAddress(any(), any())).thenReturn(
                new AddressResponse(UUID.randomUUID(), "221B Baker St", null, "Bengaluru", "Karnataka",
                        "560001", "India", AddressType.SHIPPING, true));

        mockMvc.perform(post("/api/users/me/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void add_withMissingRequiredField_returns400() throws Exception {
        String invalidJson = """
                {"addressLine1":"", "city":"Bengaluru", "state":"Karnataka",
                 "postalCode":"560001", "country":"India", "addressType":"SHIPPING", "isDefault":false}
                """;

        mockMvc.perform(post("/api/users/me/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
