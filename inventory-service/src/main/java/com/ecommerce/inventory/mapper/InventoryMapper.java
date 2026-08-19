package com.ecommerce.inventory.mapper;

import com.ecommerce.inventory.dto.response.InventoryResponse;
import com.ecommerce.inventory.dto.response.StockMovementResponse;
import com.ecommerce.inventory.entity.InventoryItem;
import com.ecommerce.inventory.entity.StockMovement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    default InventoryResponse toInventoryResponse(InventoryItem item) {
        if (item == null) {
            return null;
        }
        return new InventoryResponse(
                item.getProductId(),
                item.getSku(),
                item.getQuantityAvailable(),
                item.getReorderThreshold(),
                item.isLowStock(),
                item.getUpdatedAt()
        );
    }

    StockMovementResponse toStockMovementResponse(StockMovement movement);
}
