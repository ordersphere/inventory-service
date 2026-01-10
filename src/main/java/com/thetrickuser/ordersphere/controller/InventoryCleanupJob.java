package com.thetrickuser.ordersphere.controller;

import com.thetrickuser.ordersphere.service.InventoryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InventoryCleanupJob {

    private final InventoryService inventoryService;

    public InventoryCleanupJob(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        inventoryService.expireReservations();
    }
}
