package com.ecommerce.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Discovery Server.
 * <p>
 * Every microservice in the platform (Config Server excluded, since it must
 * be available before services can even fetch configuration) registers
 * itself here so that the API Gateway and other services can discover each
 * other by logical service-id instead of hardcoded host:port pairs.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
