package com.procurement.backend.controller;

import com.procurement.backend.repository.InventoryRepository;
import com.procurement.backend.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalRequests = requestRepository.count();
        long pendingRequests = requestRepository.countByStatus("pending");
        
        Double totalExpenditure = requestRepository.sumActualOrEstimatedCostForPurchased();
        if (totalExpenditure == null) totalExpenditure = 0.0;

        long lowStockCount = inventoryRepository.countLowStockItems();

        stats.put("totalRequests", totalRequests);
        stats.put("pendingRequests", pendingRequests);
        stats.put("totalExpenditure", totalExpenditure);
        stats.put("lowStockCount", lowStockCount);

        return stats;
    }
}
