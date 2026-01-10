package com.thetrickuser.ordersphere.service;

import com.thetrickuser.ordersphere.domain.InventoryResponse;
import com.thetrickuser.ordersphere.domain.ReserveInventoryRequest;

import java.util.Optional;

public interface InventoryService {

    InventoryResponse updateInventory(String productId, int quantity);

    void reserveInventory(ReserveInventoryRequest request);

    void releaseInventory(String orderId);

    void expireReservations();
}

