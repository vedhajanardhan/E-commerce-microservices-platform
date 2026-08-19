package com.ecommerce.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Centralized Configuration Server.
 * <p>
 * Backed by the classpath "native" profile (config-repo/ directory bundled
 * inside this service's own jar) rather than a remote Git repository. This
 * keeps the whole platform runnable offline with {@code docker compose up}
 * and avoids introducing a dependency on external Git hosting for local
 * development or grading/demo environments.
 * <p>
 * To point this at a real Git-backed config repo in production, swap the
 * {@code spring.profiles.active=native} + {@code spring.cloud.config.server.native.*}
 * properties in application.yml for {@code spring.cloud.config.server.git.uri}.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
