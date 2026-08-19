package com.ecommerce.user.controller;

import com.ecommerce.user.dto.request.AddressRequest;
import com.ecommerce.user.dto.response.AddressResponse;
import com.ecommerce.user.security.CurrentUserProvider;
import com.ecommerce.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Addresses", description = "Manage the authenticated user's shipping/billing addresses")
@RestController
@RequestMapping("/api/users/me/addresses")
public class AddressController {

    private final AddressService addressService;
    private final CurrentUserProvider currentUserProvider;

    public AddressController(AddressService addressService, CurrentUserProvider currentUserProvider) {
        this.addressService = addressService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "List all addresses for the current user")
    @GetMapping
    public ResponseEntity<List<AddressResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(addressService.listAddresses(currentUserProvider.getUserId(authentication)));
    }

    @Operation(summary = "Add a new address for the current user")
    @PostMapping
    public ResponseEntity<AddressResponse> add(Authentication authentication, @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.addAddress(currentUserProvider.getUserId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update an existing address owned by the current user")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> update(
            Authentication authentication,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(
                addressService.updateAddress(currentUserProvider.getUserId(authentication), addressId, request));
    }

    @Operation(summary = "Delete an address owned by the current user")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID addressId) {
        addressService.deleteAddress(currentUserProvider.getUserId(authentication), addressId);
        return ResponseEntity.noContent().build();
    }
}
