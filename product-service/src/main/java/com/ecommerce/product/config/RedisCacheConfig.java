package com.ecommerce.product.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Cache-aside pattern: ProductServiceImpl checks Redis first on reads,
 * falls back to Postgres on a miss and repopulates the cache, and
 * explicitly evicts on writes (see @CacheEvict on create/update/delete).
 * <p>
 * Different caches get different TTLs — individual products change
 * rarely once published (30 min TTL is fine), while search result pages
 * change more often as new products are added (5 min TTL).
 * <p>
 * The bean below only activates when {@code spring.cache.type=redis}
 * (the default in application.yml). The test profile sets
 * {@code spring.cache.type=simple}, so tests run against Spring Boot's
 * auto-configured in-memory ConcurrentMapCacheManager instead — caching
 * behavior stays exercised without requiring a live Redis instance in CI.
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    private static final String PRODUCT_CACHE = "products";
    private static final String PRODUCT_LIST_CACHE = "productLists";
    private static final String CATEGORY_CACHE = "categories";

    @Bean
    @ConditionalOnProperty(prefix = "spring.cache", name = "type", havingValue = "redis", matchIfMissing = true)
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                PRODUCT_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(30)),
                PRODUCT_LIST_CACHE, defaultConfig.entryTtl(Duration.ofMinutes(5)),
                CATEGORY_CACHE, defaultConfig.entryTtl(Duration.ofHours(1))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
