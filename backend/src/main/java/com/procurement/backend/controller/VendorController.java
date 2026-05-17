package com.procurement.backend.controller;

import com.procurement.backend.entity.Vendor;
import com.procurement.backend.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
public class VendorController {

    @Autowired
    private VendorRepository vendorRepository;

    @GetMapping
    public List<Vendor> getVendors() {
        return vendorRepository.findAll();
    }

    @PostMapping
    public Vendor addVendor(@RequestBody Vendor vendor) {
        if (vendor.getId() == null) {
            vendor.setId(UUID.randomUUID().toString());
        }
        return vendorRepository.save(vendor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vendor> updateVendor(@PathVariable String id, @RequestBody Vendor updatedVendor) {
        return vendorRepository.findById(id).map(vendor -> {
            vendor.setName(updatedVendor.getName());
            vendor.setContact(updatedVendor.getContact());
            vendor.setEmail(updatedVendor.getEmail());
            vendor.setGstNumber(updatedVendor.getGstNumber());
            vendor.setRating(updatedVendor.getRating());
            return ResponseEntity.ok(vendorRepository.save(vendor));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
