package com.ecommerce.cart.mapper;

import com.ecommerce.cart.dto.response.CartItemResponse;
import com.ecommerce.cart.dto.response.CartResponse;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    default CartItemResponse toCartItemResponse(CartItem item) {
        if (item == null) {
            return null;
        }
        return new CartItemResponse(
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }

    List<CartItemResponse> toCartItemResponseList(List<CartItem> items);

    default CartResponse toCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }
        return new CartResponse(
                cart.getUserId(),
                toCartItemResponseList(cart.getItems()),
                cart.getTotalItemCount(),
                cart.getTotal(),
                cart.getUpdatedAt()
        );
    }
}
