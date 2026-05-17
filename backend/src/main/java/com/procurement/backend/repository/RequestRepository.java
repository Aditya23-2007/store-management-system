package com.procurement.backend.repository;

import com.procurement.backend.entity.ProcurementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface RequestRepository extends JpaRepository<ProcurementRequest, String> {
    long countByStatus(String status);

    @Query("SELECT SUM(COALESCE(r.actualCost, r.estimatedCost)) FROM ProcurementRequest r WHERE r.status IN ('purchased', 'bill_verified', 'delivered')")
    Double sumActualOrEstimatedCostForPurchased();

    List<ProcurementRequest> findAllByOrderByCreatedAtDesc();
}
