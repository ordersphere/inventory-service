package com.thetrickuser.ordersphere.domain.error;

import com.ordersphere.core.api.ErrorCode;

public enum InventoryErrors {
    INVENTORY_NOT_FOUND(new ErrorCode(404, 1002, 0001)),
    INVALID_INVENTORY_RESERVATION_REQUEST(new ErrorCode(400, 1002, 0002));

    private final ErrorCode code;

    InventoryErrors(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
