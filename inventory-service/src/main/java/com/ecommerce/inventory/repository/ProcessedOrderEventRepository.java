package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.ProcessedOrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedOrderEventRepository extends JpaRepository<ProcessedOrderEvent, UUID> {
}
