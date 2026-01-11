package com.thetrickuser.ordersphere.exception;

import com.ordersphere.core.exception.BaseException;
import com.thetrickuser.ordersphere.domain.error.InventoryErrors;

public class InventoryReservationFailedException extends BaseException {
    public InventoryReservationFailedException(String message) {
        super(message, InventoryErrors.INVALID_INVENTORY_RESERVATION_REQUEST.code());
    }
}
