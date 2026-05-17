package com.procurement.backend.controller;

import com.procurement.backend.entity.ProcurementRequest;
import com.procurement.backend.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class RequestController {

    @Autowired
    private RequestRepository requestRepository;

    @GetMapping
    public List<ProcurementRequest> getAllRequests() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcurementRequest> getRequestById(@PathVariable String id) {
        Optional<ProcurementRequest> req = requestRepository.findById(id);
        return req.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProcurementRequest createRequest(@RequestBody ProcurementRequest request) {
        if (request.getId() == null) {
            request.setId(UUID.randomUUID().toString());
        }
        if (request.getStatus() == null) {
            request.setStatus("pending");
        }
        return requestRepository.save(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcurementRequest> updateRequest(@PathVariable String id, @RequestBody ProcurementRequest updatedRequest) {
        return requestRepository.findById(id).map(request -> {
            request.setItemName(updatedRequest.getItemName());
            request.setQuantity(updatedRequest.getQuantity());
            request.setEstimatedCost(updatedRequest.getEstimatedCost());
            request.setActualCost(updatedRequest.getActualCost());
            request.setPurpose(updatedRequest.getPurpose());
            request.setUrgency(updatedRequest.getUrgency());
            request.setStatus(updatedRequest.getStatus());
            request.setCategory(updatedRequest.getCategory());
            ProcurementRequest saved = requestRepository.save(request);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProcurementRequest> updateStatus(@PathVariable String id, @RequestParam String status) {
        return requestRepository.findById(id).map(request -> {
            request.setStatus(status);
            ProcurementRequest saved = requestRepository.save(request);
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable String id) {
        if (requestRepository.existsById(id)) {
            requestRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
