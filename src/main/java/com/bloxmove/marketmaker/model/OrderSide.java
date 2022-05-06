package com.bloxmove.marketmaker.model;

public enum OrderSide {
    BUY,
    SELL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
