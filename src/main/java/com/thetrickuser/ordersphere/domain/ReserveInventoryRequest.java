package com.thetrickuser.ordersphere.domain;

import java.util.List;

public record ReserveInventoryRequest(
        String orderId,
        List<ReserveItem> items,
        int ttlMinutes
) {}

