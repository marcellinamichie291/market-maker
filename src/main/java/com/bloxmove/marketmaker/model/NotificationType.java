package com.bloxmove.marketmaker.model;

public enum NotificationType {
    PLACE_ORDER_FAIL("Placing order '%s' failed"),
    CANCEL_ORDERS_FAIL("Cancel orders for side '%s' failed"),
    LOW_BALANCE("Balance of '%s' is too low");

    private final String message;

    NotificationType(String message) {
        this.message = message;
    }

    public String getMessage(String parameter) {
        return String.format(message, parameter);
    }
}
