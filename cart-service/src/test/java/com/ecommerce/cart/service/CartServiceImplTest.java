package com.ecommerce.cart.service;

import com.ecommerce.cart.client.InventoryClient;
import com.ecommerce.cart.client.ProductInfo;
import com.ecommerce.cart.client.ProductServiceClient;
import com.ecommerce.cart.client.StockValidationResponse;
import com.ecommerce.cart.dto.request.AddItemRequest;
import com.ecommerce.cart.dto.response.CartResponse;
import com.ecommerce.cart.exception.CartItemNotFoundException;
import com.ecommerce.cart.exception.InsufficientStockException;
import com.ecommerce.cart.exception.ProductNotAvailableException;
import com.ecommerce.cart.mapper.CartMapper;
import com.ecommerce.cart.mapper.CartMapperImpl;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private ProductServiceClient productServiceClient;
    @Mock private InventoryClient inventoryClient;

    // Using the real generated-style mapper (hand-instantiated, since we
    // don't spin up Spring context here) keeps these tests honest about
    // the actual response shape instead of mocking mapper output.
    private final CartMapper cartMapper = new CartMapperImpl();

    private CartServiceImpl cartService;
    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(cartRepository, productServiceClient, inventoryClient, cartMapper);
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    @Test
    void addItem_newProduct_addsToEmptyCart() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(productServiceClient.getProductById(productId)).thenReturn(
                new ProductInfo(productId, "SKU-1", "Wireless Mouse", new BigDecimal("999.00"), true));
        when(inventoryClient.validateStock(any())).thenReturn(new StockValidationResponse(true, java.util.List.of()));

        CartResponse response = cartService.addItem(userId, new AddItemRequest(productId, 2));

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());
        assertEquals(new BigDecimal("1998.00"), response.total());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_existingProduct_increasesQuantity() {
        Cart existingCart = Cart.empty(userId);
        existingCart.getItems().add(com.ecommerce.cart.model.CartItem.builder()
                .productId(productId).sku("SKU-1").productName("Wireless Mouse")
                .unitPrice(new BigDecimal("999.00")).quantity(1).build());
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(productServiceClient.getProductById(productId)).thenReturn(
                new ProductInfo(productId, "SKU-1", "Wireless Mouse", new BigDecimal("999.00"), true));
        when(inventoryClient.validateStock(any())).thenReturn(new StockValidationResponse(true, java.util.List.of()));

        CartResponse response = cartService.addItem(userId, new AddItemRequest(productId, 2));

        assertEquals(1, response.items().size());
        assertEquals(3, response.items().get(0).quantity());
    }

    @Test
    void addItem_inactiveProduct_throwsProductNotAvailableException() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(productServiceClient.getProductById(productId)).thenReturn(
                new ProductInfo(productId, "SKU-1", "Discontinued Item", new BigDecimal("10.00"), false));

        assertThrows(ProductNotAvailableException.class,
                () -> cartService.addItem(userId, new AddItemRequest(productId, 1)));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItem_insufficientStock_throwsInsufficientStockException() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(productServiceClient.getProductById(productId)).thenReturn(
                new ProductInfo(productId, "SKU-1", "Popular Item", new BigDecimal("10.00"), true));
        when(inventoryClient.validateStock(any())).thenReturn(
                new StockValidationResponse(false, java.util.List.of(productId)));

        assertThrows(InsufficientStockException.class,
                () -> cartService.addItem(userId, new AddItemRequest(productId, 100)));
        verify(cartRepository, never()).save(any());
    }

    @Test
    void updateQuantity_whenItemNotInCart_throwsCartItemNotFoundException() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(Cart.empty(userId)));

        assertThrows(CartItemNotFoundException.class,
                () -> cartService.updateQuantity(userId, productId, 3));
    }

    @Test
    void removeItem_removesFromCart() {
        Cart existingCart = Cart.empty(userId);
        existingCart.getItems().add(com.ecommerce.cart.model.CartItem.builder()
                .productId(productId).sku("SKU-1").productName("Item")
                .unitPrice(BigDecimal.TEN).quantity(1).build());
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));

        CartResponse response = cartService.removeItem(userId, productId);

        assertTrue(response.items().isEmpty());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void clearCart_deletesFromRepository() {
        cartService.clearCart(userId);
        verify(cartRepository).deleteByUserId(userId);
    }
}
