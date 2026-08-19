package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    /**
     * Pessimistic write lock for the read-modify-write cycle in
     * increase/decrease operations, on top of the optimistic
     * {@code @Version} field already on the entity. Belt-and-braces:
     * the pessimistic lock avoids the lost-update race entirely for
     * high-contention products (e.g. a flash sale item), while
     * @Version still protects any code path that reads without locking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryItem i WHERE i.productId = :productId")
    Optional<InventoryItem> findByIdForUpdate(UUID productId);
}
