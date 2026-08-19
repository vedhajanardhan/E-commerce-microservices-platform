package com.ecommerce.product.service.impl;

import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.request.ProductSearchRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductImage;
import com.ecommerce.product.exception.DuplicateSkuException;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.ProductSpecifications;
import com.ecommerce.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Cache-aside implementation:
 * <ul>
 *   <li>{@code getProductById} — long-lived (30 min), targeted eviction by
 *       id on update/delete. Individual product pages are read far more
 *       often than they're written, so this is where caching pays off
 *       the most.</li>
 *   <li>{@code searchProducts} — short-lived (5 min), coarse eviction
 *       (entire {@code productLists} cache cleared on any write). Trying
 *       to selectively invalidate every filter/page/sort combination
 *       that could contain a changed product isn't worth the complexity;
 *       a short TTL bounds staleness instead.</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "productLists", key = "T(java.util.Objects).hash(#searchRequest.keyword(), #searchRequest.categoryId(), #searchRequest.brand(), #searchRequest.minPrice(), #searchRequest.maxPrice(), #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    public Page<ProductResponse> searchProducts(ProductSearchRequest searchRequest, Pageable pageable) {
        log.debug("Cache miss: executing product search against DB, filters={}", searchRequest);
        var spec = ProductSpecifications.withFilters(
                searchRequest.keyword(),
                searchRequest.categoryId(),
                searchRequest.brand(),
                searchRequest.minPrice(),
                searchRequest.maxPrice(),
                true // storefront search only ever returns active products
        );
        return productRepository.findAll(spec, pageable).map(productMapper::toProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(UUID id) {
        log.debug("Cache miss: loading product {} from DB", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return productMapper.toProductResponse(product);
    }

    @Override
    @CacheEvict(value = "productLists", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException("A product with SKU '" + request.sku() + "' already exists");
        }
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(category)
                .brand(request.brand())
                .active(request.active() == null || request.active())
                .build();

        attachImages(product, request);

        Product saved = productRepository.save(product);
        log.info("Product created: id={}, sku={}", saved.getId(), saved.getSku());
        return productMapper.toProductResponse(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "productLists", allEntries = true)
    })
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        if (!product.getSku().equals(request.sku()) && productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException("A product with SKU '" + request.sku() + "' already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setBrand(request.brand());
        if (request.active() != null) {
            product.setActive(request.active());
        }

        product.getImages().clear();
        attachImages(product, request);

        Product saved = productRepository.save(product);
        log.info("Product updated: id={}", saved.getId());
        return productMapper.toProductResponse(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "productLists", allEntries = true)
    })
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: id={}", id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "productLists", allEntries = true)
    })
    public ProductResponse setActive(UUID id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setActive(active);
        Product saved = productRepository.save(product);
        log.info("Product {} set active={}", id, active);
        return productMapper.toProductResponse(saved);
    }

    private void attachImages(Product product, ProductRequest request) {
        if (request.imageUrls() == null) {
            return;
        }
        int order = 0;
        for (String url : request.imageUrls()) {
            product.addImage(ProductImage.builder().url(url).displayOrder(order++).build());
        }
    }
}
