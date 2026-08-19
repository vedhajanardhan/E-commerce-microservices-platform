package com.ecommerce.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * User Profile & Address Service.
 * <p>
 * Owns the user_profiles and addresses tables (database-per-service).
 * Identity/credentials live in auth-service; this service fetches that
 * data synchronously via OpenFeign ({@link com.ecommerce.user.client.AuthServiceClient})
 * and merges it with the extended profile data it owns.
 */
@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
