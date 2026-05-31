package com.gamemall.order;

public enum OrderStatus {
    CREATING(5),
    PENDING_PAYMENT(10),
    PAID(20),
    CANCELLED(30),
    CLOSED(40);

    private final int code;

    OrderStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
