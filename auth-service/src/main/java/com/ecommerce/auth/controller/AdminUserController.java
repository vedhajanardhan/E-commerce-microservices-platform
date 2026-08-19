package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.response.UserResponse;
import com.ecommerce.auth.exception.ResourceNotFoundException;
import com.ecommerce.auth.mapper.UserMapper;
import com.ecommerce.auth.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * All routes here are under /api/auth/admin/** which SecurityConfig
 * restricts to ROLE_ADMIN. This lets the gateway's coarse-grained
 * "/admin/" path check and this service's fine-grained hasRole("ADMIN")
 * check reinforce each other.
 */
@Tag(name = "Admin - Users", description = "Admin-only user management")
@RestController
@RequestMapping("/api/auth/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AdminUserController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Operation(summary = "List all registered users (paginated)")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userRepository.findAll(pageable).map(userMapper::toUserResponse));
    }

    @Operation(summary = "Get a specific user by id")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @Operation(summary = "Disable a user account (e.g. for policy violations)")
    @PatchMapping("/{userId}/disable")
    public ResponseEntity<UserResponse> disableUser(@PathVariable UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    @Operation(summary = "Re-enable a previously disabled user account")
    @PatchMapping("/{userId}/enable")
    public ResponseEntity<UserResponse> enableUser(@PathVariable UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }
}
