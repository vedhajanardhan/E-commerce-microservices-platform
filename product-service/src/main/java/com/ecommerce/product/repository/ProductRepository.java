package com.ecommerce.product.repository;

import com.ecommerce.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Extends {@link JpaSpecificationExecutor} so ProductServiceImpl can build
 * dynamic search queries (by name, category, brand, price range, active
 * status) without hand-writing a combinatorial explosion of @Query methods.
 */
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    boolean existsBySku(String sku);

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
}
