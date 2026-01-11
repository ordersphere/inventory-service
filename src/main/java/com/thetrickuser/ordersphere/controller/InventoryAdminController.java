package com.thetrickuser.ordersphere.controller;

import com.thetrickuser.ordersphere.domain.InventoryResponse;
import com.thetrickuser.ordersphere.domain.UpdateInventoryRequest;
import com.thetrickuser.ordersphere.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/inventory")
public class InventoryAdminController {

    private static final Logger log = LoggerFactory.getLogger(InventoryAdminController.class);
    private final InventoryService inventoryService;

    public InventoryAdminController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PutMapping("/{productId}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable String productId,
            @RequestBody UpdateInventoryRequest request
    ) {
        log.info("Received inventory update request for productId: {}, new available quantity: {}", productId, request.quantity());
        InventoryResponse inventoryResponse = inventoryService.updateInventory(productId, request.quantity());
        return ResponseEntity.ok(inventoryResponse);
    }
}
