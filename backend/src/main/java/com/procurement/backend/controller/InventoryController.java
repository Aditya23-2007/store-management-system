package com.procurement.backend.controller;

import com.procurement.backend.entity.InventoryItem;
import com.procurement.backend.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public List<InventoryItem> getInventory() {
        return inventoryRepository.findAll();
    }

    @PostMapping
    public InventoryItem addItem(@RequestBody InventoryItem item) {
        if (item.getId() == null) {
            item.setId(UUID.randomUUID().toString());
        }
        return inventoryRepository.save(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable String id, @RequestBody InventoryItem updatedItem) {
        return inventoryRepository.findById(id).map(item -> {
            item.setName(updatedItem.getName());
            item.setCategory(updatedItem.getCategory());
            item.setQuantity(updatedItem.getQuantity());
            item.setUnit(updatedItem.getUnit());
            item.setLowStockThreshold(updatedItem.getLowStockThreshold());
            item.setSku(updatedItem.getSku());
            return ResponseEntity.ok(inventoryRepository.save(item));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<InventoryItem> adjustStock(@PathVariable String id, @RequestParam Integer quantity) {
        return inventoryRepository.findById(id).map(item -> {
            item.setQuantity(quantity);
            return ResponseEntity.ok(inventoryRepository.save(item));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
