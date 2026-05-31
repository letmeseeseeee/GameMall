package com.gamemall.order;

public enum PaymentStatus {
    UNPAID(0),
    PAID(1),
    REFUNDED(2);

    private final int code;

    PaymentStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
