package com.procurement.backend.repository;

import com.procurement.backend.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryRepository extends JpaRepository<InventoryItem, String> {
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.quantity < i.lowStockThreshold")
    long countLowStockItems();
}
