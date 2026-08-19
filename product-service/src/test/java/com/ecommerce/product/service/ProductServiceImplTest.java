package com.ecommerce.product.service;

import com.ecommerce.product.dto.request.ProductRequest;
import com.ecommerce.product.dto.response.ProductResponse;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.DuplicateSkuException;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Electronics").build();
        request = new ProductRequest("SKU-001", "Wireless Mouse", "A mouse",
                new BigDecimal("999.00"), 1L, "Logitech", true, List.of("http://img.com/1.jpg"));
    }

    @Test
    void createProduct_withNewSku_savesProduct() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Product saved = Product.builder().id(UUID.randomUUID()).sku("SKU-001").name("Wireless Mouse")
                .category(category).price(new BigDecimal("999.00")).build();
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse expected = new ProductResponse(saved.getId(), "SKU-001", "Wireless Mouse", "A mouse",
                new BigDecimal("999.00"), 1L, "Electronics", "Logitech", true, List.of(),
                LocalDateTime.now(), LocalDateTime.now());
        when(productMapper.toProductResponse(saved)).thenReturn(expected);

        ProductResponse response = productService.createProduct(request);

        assertEquals("SKU-001", response.sku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_withDuplicateSku_throwsException() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_withUnknownCategory_throwsResourceNotFoundException() {
        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(id));
    }

    @Test
    void deleteProduct_whenExists_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(true);

        productService.deleteProduct(id);

        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteProduct_whenNotFound_throwsResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(id));
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void updateProduct_changingToExistingSku_throwsDuplicateSkuException() {
        UUID id = UUID.randomUUID();
        Product existing = Product.builder().id(id).sku("SKU-OLD").category(category).build();
        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> productService.updateProduct(id, request));
    }
}
