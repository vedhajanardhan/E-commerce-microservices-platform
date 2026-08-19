package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.ProcessedOrderCancellation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedOrderCancellationRepository extends JpaRepository<ProcessedOrderCancellation, UUID> {
}
