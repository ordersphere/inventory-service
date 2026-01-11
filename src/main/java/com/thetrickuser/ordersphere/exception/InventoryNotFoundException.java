package com.thetrickuser.ordersphere.exception;

import com.ordersphere.core.exception.BaseException;
import com.thetrickuser.ordersphere.domain.error.InventoryErrors;

public class InventoryNotFoundException extends BaseException {
    public InventoryNotFoundException(String message) {
        super(message, InventoryErrors.INVENTORY_NOT_FOUND.code());
    }
}
