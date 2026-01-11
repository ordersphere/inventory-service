package com.thetrickuser.ordersphere.controller;

import com.thetrickuser.ordersphere.domain.ReserveInventoryRequest;
import com.thetrickuser.ordersphere.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveInventory(
            @RequestBody ReserveInventoryRequest request
    ) {
        log.info("Received inventory reservation request for orderId: {}", request.orderId());
        inventoryService.reserveInventory(request);
        return ResponseEntity.ok("Inventory reserved successfully");
    }

    @PostMapping("/release")
    public ResponseEntity<String> releaseInventory(
            @RequestParam String orderId
    ) {
        log.info("Received inventory release request for orderId: {}", orderId);
        inventoryService.releaseInventory(orderId);
        return ResponseEntity.ok("Inventory released successfully");
    }
}

