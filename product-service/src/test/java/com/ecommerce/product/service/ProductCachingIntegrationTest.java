package com.ecommerce.product.service;

import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Verifies the cache-aside contract end to end: the first call to
 * getProductById() must hit the repository, the second call for the
 * same id must NOT — proving @Cacheable is actually short-circuiting
 * the DB round trip, not just present as an unused annotation.
 */
@SpringBootTest
class ProductCachingIntegrationTest {

    @Autowired private ProductService productService;
    @Autowired private CategoryRepository categoryRepository;

    @SpyBean
    private ProductRepository productRepository;

    @Test
    void getProductById_secondCall_servedFromCacheNotDatabase() {
        Category category = categoryRepository.save(Category.builder().name("Test Category " + UUID.randomUUID()).build());
        Product product = productRepository.save(Product.builder()
                .sku("CACHE-TEST-" + UUID.randomUUID())
                .name("Cached Product")
                .price(new BigDecimal("49.99"))
                .category(category)
                .build());

        var first = productService.getProductById(product.getId());
        var second = productService.getProductById(product.getId());

        assertEquals(first.id(), second.id());
        // findById should only have been invoked once across both calls —
        // the second was served entirely from the "products" cache.
        verify(productRepository, times(1)).findById(product.getId());
    }
}
