package com.thetrickuser.ordersphere.controller;

import com.thetrickuser.ordersphere.domain.ReserveInventoryRequest;
import com.thetrickuser.ordersphere.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveInventory(
            @RequestBody ReserveInventoryRequest request
    ) {
        inventoryService.reserveInventory(request);
        return ResponseEntity.ok("Inventory reserved successfully");
    }

    @PostMapping("/release")
    public ResponseEntity<String> releaseInventory(
            @RequestParam String orderId
    ) {
        inventoryService.releaseInventory(orderId);
        return ResponseEntity.ok("Inventory released successfully");
    }
}

