package com.thetrickuser.ordersphere.domain;

public record InventoryResponse(
        String productId,
        int availableQuantity,
        int reservedQuantity
) {}
